package com.visura.database.repository;

import com.visura.database.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * A fila de tarefas, só pelas rotinas do banco. Cada chamada é uma transação curta: a reserva confirma antes de a
 * tarefa rodar, para não prender conexão nem trava durante o processamento.
 */
public interface TaskRepository extends JpaRepository<Task, Integer> {

    @Transactional
    @Query(value = "select processamento.enfileirar_tarefa(:type, :reference)", nativeQuery = true)
    UUID enqueue(@Param("type") String type, @Param("reference") UUID reference);

    /** A próxima tarefa disponível, já marcada como em execução; {@code lease} em segundos. */
    @Transactional
    @Query(value = "select * from processamento.reservar_tarefa(:lease)", nativeQuery = true)
    Optional<Task> reserve(@Param("lease") int lease);

    @Transactional
    @Procedure(procedureName = "processamento.concluir_tarefa")
    void conclude(@Param("p_id_tarefa") Integer task);

    /** Devolve a tarefa à fila depois de {@code delay} segundos, ou a falha de vez se esgotou as tentativas. */
    @Transactional
    @Procedure(procedureName = "processamento.adiar_tarefa")
    void postpone(@Param("p_id_tarefa") Integer task, @Param("p_erro") String error, @Param("p_espera") int delay);
}
