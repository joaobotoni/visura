package com.visura.controller;

import com.visura.service.TaskService;
import com.visura.support.ApplicationTest;
import com.visura.support.Controle;
import com.visura.task.TaskHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A fila de tarefas com a aplicação inteira: mapeamento JPA, chamadas às rotinas do banco e o processador do tipo.
 */
@DisplayName("Fluxo da fila de tarefas")
@Import(TaskFlowTest.Handlers.class)
class TaskFlowTest extends ApplicationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private Handlers handlers;

    @Test
    @Controle
    @DisplayName("tarefa enfileirada é processada pelo processador do tipo e concluída")
    void enqueuedTaskIsProcessed() throws Exception {
        UUID reference = UUID.randomUUID();

        UUID task = taskService.enqueue("teste_fluxo", reference);
        processed(task);

        assertThat(handlers.references).contains(reference);
        assertThat(text("select status::text from processamento.tarefa where uuid = '" + task + "'")).isEqualTo("concluida");
    }

    @Test
    @Controle
    @DisplayName("falha do processador devolve a tarefa à fila com o erro")
    void failingTaskIsPostponed() throws Exception {
        UUID task = taskService.enqueue("teste_falha", UUID.randomUUID());
        settled(task, "pendente:1");

        assertThat(text("select tentativas::text || ':' || erro from processamento.tarefa where uuid = '" + task + "'"))
                .startsWith("1:").contains("sem conexão com o provedor");
        // O banco é compartilhado pelos testes: a tarefa sai da fila para outros contextos não a tentarem de novo
        execute("update processamento.tarefa set status = 'falhou' where uuid = '" + task + "'");
    }

    /** Processa a fila até a tarefa ser concluída (o agendador da aplicação também pode pegá-la antes). */
    private void processed(UUID task) throws Exception {
        settled(task, "concluida:1");
    }

    private void settled(UUID task, String status) throws Exception {
        for (int round = 0; round < 50 && !status.equals(state(task)); round++) {
            taskService.work();
            Thread.sleep(100);
        }
    }

    /** Situação e tentativas, como "pendente:1". */
    private static String state(UUID task) throws Exception {
        return text("select status || ':' || tentativas from processamento.tarefa where uuid = '" + task + "'");
    }

    @TestConfiguration
    static class Handlers {

        private final List<UUID> references = new CopyOnWriteArrayList<>();

        @Bean
        TaskHandler recorder() {
            return new TaskHandler() {
                @Override
                public String type() {
                    return "teste_fluxo";
                }

                @Override
                public void handle(UUID reference) {
                    references.add(reference);
                }
            };
        }

        @Bean
        TaskHandler failing() {
            return new TaskHandler() {
                @Override
                public String type() {
                    return "teste_falha";
                }

                @Override
                public void handle(UUID reference) {
                    throw new IllegalStateException("sem conexão com o provedor");
                }
            };
        }
    }
}
