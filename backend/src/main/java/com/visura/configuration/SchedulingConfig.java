package com.visura.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Tarefas periódicas da aplicação (limpeza de arquivos órfãos de evidência e a fila de tarefas em segundo plano).
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
