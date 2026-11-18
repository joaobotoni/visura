package com.visura.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

/**
 * Tarefa da fila de processamento em segundo plano, como devolvida pela reserva. Só leitura: a fila muda apenas pelas
 * rotinas do banco ({@code processamento.*}).
 */
@Entity
@Getter
@Immutable
@Table(schema = "processamento", name = "tarefa")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task {

    @Id
    @Column(name = "id_tarefa")
    private Integer id;

    @Column(name = "uuid", nullable = false)
    private UUID uuid;

    @Column(name = "tipo", nullable = false)
    private String type;

    @Column(name = "referencia", nullable = false)
    private UUID reference;

    @Column(name = "tentativas", nullable = false)
    private int attempts;
}
