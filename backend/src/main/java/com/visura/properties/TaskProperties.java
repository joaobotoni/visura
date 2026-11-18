package com.visura.properties;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Fila de tarefas em segundo plano: intervalo entre as buscas ({@code poll}), prazo depois do qual uma tarefa em
 * execução é considerada abandonada ({@code lease}), quantas tarefas cada busca processa ({@code batch}) e a espera
 * antes de tentar de novo, que dobra a cada falha ({@code backoff}) até o teto ({@code ceiling}).
 */
@Validated
@ConfigurationProperties(prefix = "vsr.task")
public record TaskProperties(@NotNull Duration poll,
                             @NotNull Duration lease,
                             @Positive int batch,
                             @NotNull Duration backoff,
                             @NotNull Duration ceiling) {
}
