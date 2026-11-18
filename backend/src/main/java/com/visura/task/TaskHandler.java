package com.visura.task;

import java.util.UUID;

/**
 * Quem processa um tipo de tarefa da fila (por exemplo, a transcrição de um áudio de evidência). Cada implementação é
 * um bean do Spring; o {@code TaskService} entrega a ela as tarefas do seu {@link #type()}.
 * <p>
 * O processamento precisa ser <b>idempotente</b>: se a instância cair no meio, a mesma tarefa roda de novo. Lançar uma
 * exceção devolve a tarefa à fila, com espera crescente, até esgotar as tentativas.
 */
public interface TaskHandler {

    /** O tipo de tarefa atendido: minúsculas, números e "_", até 50 caracteres (ex.: {@code transcricao}). */
    String type();

    /** Processa o recurso referenciado pela tarefa (ex.: o uuid da evidência). */
    void handle(UUID reference);
}
