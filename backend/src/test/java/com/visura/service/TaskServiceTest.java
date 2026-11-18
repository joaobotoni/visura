package com.visura.service;

import com.visura.database.entity.Task;
import com.visura.database.repository.TaskRepository;
import com.visura.properties.TaskProperties;
import com.visura.support.Controle;
import com.visura.task.TaskHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Fila de tarefas")
class TaskServiceTest {

    private static final TaskProperties PROPERTIES = new TaskProperties(Duration.ofSeconds(5), Duration.ofMinutes(15), 3,
            Duration.ofSeconds(30), Duration.ofHours(1));

    private final TaskRepository repository = mock(TaskRepository.class);
    private final Recorder handler = new Recorder("teste");

    @Test
    @Controle
    @DisplayName("entrega a tarefa ao processador do tipo e conclui")
    void runsAndConcludes() {
        Task task = task(1, "teste", 1);
        when(repository.reserve(900)).thenReturn(Optional.of(task), Optional.empty());

        service(handler).work();

        assertThat(handler.references).containsExactly(task.getReference());
        verify(repository).conclude(1);
        verify(repository, never()).postpone(anyInt(), anyString(), anyInt());
    }

    @Test
    @Controle
    @DisplayName("falha devolve a tarefa à fila com o erro, sem concluir")
    void failurePostpones() {
        Task task = task(2, "teste", 1);
        when(repository.reserve(900)).thenReturn(Optional.of(task), Optional.empty());
        handler.failing = true;

        service(handler).work();

        verify(repository).postpone(eq(2), contains("falhou de propósito"), eq(30));
        verify(repository, never()).conclude(anyInt());
    }

    @Test
    @Controle
    @DisplayName("tipo sem processador é uma falha como outra qualquer: volta à fila até esgotar")
    void unknownTypePostpones() {
        Task task = task(3, "desconhecido", 1);
        when(repository.reserve(900)).thenReturn(Optional.of(task), Optional.empty());

        service(handler).work();

        verify(repository).postpone(eq(3), contains("desconhecido"), eq(30));
    }

    @Test
    @Controle
    @DisplayName("cada busca processa no máximo batch tarefas")
    void respectsBatch() {
        List<Optional<Task>> tasks = List.of(Optional.of(task(1, "teste", 1)), Optional.of(task(2, "teste", 1)),
                Optional.of(task(3, "teste", 1)), Optional.of(task(4, "teste", 1)));
        when(repository.reserve(900)).thenReturn(tasks.get(0), tasks.get(1), tasks.get(2), tasks.get(3));

        service(handler).work();

        verify(repository, times(3)).reserve(900);
        assertThat(handler.references).hasSize(3);
    }

    @ParameterizedTest(name = "tentativa {0}: espera {1} s")
    @Controle
    @DisplayName("a espera dobra a cada falha, até o teto")
    @CsvSource({"1, 30", "2, 60", "3, 120", "4, 240", "7, 1920", "8, 3600", "20, 3600"})
    void backoffDoublesUntilCeiling(int attempts, int seconds) {
        Task task = task(9, "teste", attempts);
        when(repository.reserve(900)).thenReturn(Optional.of(task), Optional.empty());
        handler.failing = true;

        service(handler).work();

        verify(repository).postpone(eq(9), anyString(), eq(seconds));
    }

    @Test
    @Controle
    @DisplayName("dois processadores do mesmo tipo impedem a aplicação de iniciar")
    void duplicateTypesFail() {
        assertThatThrownBy(() -> service(handler, new Recorder("teste"))).isInstanceOf(IllegalStateException.class);
    }

    private TaskService service(TaskHandler... handlers) {
        return new TaskService(repository, PROPERTIES, List.of(handlers));
    }

    private static Task task(int id, String type, int attempts) {
        Task task = mock(Task.class);
        when(task.getId()).thenReturn(id);
        when(task.getUuid()).thenReturn(UUID.randomUUID());
        when(task.getType()).thenReturn(type);
        when(task.getReference()).thenReturn(UUID.randomUUID());
        when(task.getAttempts()).thenReturn(attempts);
        return task;
    }

    private static final class Recorder implements TaskHandler {

        private final String type;
        private final List<UUID> references = new ArrayList<>();
        private boolean failing;

        private Recorder(String type) {
            this.type = type;
        }

        @Override
        public String type() {
            return type;
        }

        @Override
        public void handle(UUID reference) {
            references.add(reference);
            if (failing) {
                throw new IllegalStateException("falhou de propósito");
            }
        }
    }
}
