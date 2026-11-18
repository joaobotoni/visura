package com.visura.service;

import com.visura.database.entity.Task;
import com.visura.database.repository.TaskRepository;
import com.visura.properties.TaskProperties;
import com.visura.task.TaskHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Fila de tarefas em segundo plano (transcrição, análise pela IA, laudo). A cada {@code vsr.task.poll}, reserva até
 * {@code batch} tarefas e entrega cada uma ao {@link TaskHandler} do tipo dela, fora de transação. Deu certo, conclui;
 * falhou, devolve à fila com espera que dobra a cada tentativa, até {@code ceiling}. As regras de estado (reserva sem
 * disputa entre instâncias, tentativas esgotadas, tarefa abandonada) ficam no banco.
 */
@Slf4j
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskProperties taskProperties;
    private final Map<String, TaskHandler> handlers;

    public TaskService(TaskRepository taskRepository, TaskProperties taskProperties, List<TaskHandler> handlers) {
        this.taskRepository = taskRepository;
        this.taskProperties = taskProperties;
        this.handlers = handlers.stream().collect(Collectors.toUnmodifiableMap(TaskHandler::type, Function.identity()));
    }

    /** Põe na fila o processamento de {@code reference} pelo {@link TaskHandler} de {@code type}; devolve o id da tarefa. */
    public UUID enqueue(String type, UUID reference) {
        return task(type, reference);
    }

    @Scheduled(fixedDelayString = "${vsr.task.poll}")
    public void work() {
        Stream.generate(this::reserve)
                .limit(taskProperties.batch())
                .takeWhile(Optional::isPresent)
                .map(Optional::get)
                .forEach(this::run);
    }

    private UUID task(String type, UUID reference) {
        return taskRepository.enqueue(type, reference);
    }

    private Optional<Task> reserve() {
        return taskRepository.reserve(seconds(taskProperties.lease()));
    }

    private void conclude(Task task) {
        taskRepository.conclude(task.getId());
    }

    private void postpone(Task task, RuntimeException exception) {
        log.warn("Tarefa {} ({}) falhou na tentativa {}: {}", task.getUuid(), task.getType(), task.getAttempts(), exception.toString());
        taskRepository.postpone(task.getId(), exception.toString(), seconds(delay(task.getAttempts())));
    }

    /** Espera antes da próxima tentativa: {@code backoff} na primeira falha, dobrando a cada falha, até {@code ceiling}. */
    private Duration delay(int attempts) {
        Duration growing = taskProperties.backoff().multipliedBy(1L << Math.min(Math.max(attempts - 1, 0), 20));
        return growing.compareTo(taskProperties.ceiling()) > 0 ? taskProperties.ceiling() : growing;
    }

    private static int seconds(Duration duration) {
        return Math.toIntExact(duration.toSeconds());
    }

    private void run(Task task) {
        try {
            handler(task.getType()).handle(task.getReference());
            conclude(task);
        } catch (RuntimeException exception) {
            postpone(task, exception);
        }
    }

    private TaskHandler handler(String type) {
        TaskHandler handler = handlers.get(type);
        if (handler == null) {
            throw new IllegalStateException("Nenhum processador para tarefas do tipo " + type + ".");
        }
        return handler;
    }
}
