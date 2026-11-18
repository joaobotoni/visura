package com.visura.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;
import java.time.Duration;

/**
 * Diretório dos arquivos das evidências ({@code EVIDENCE_DIR}, obrigatório só com {@code EVIDENCE_STORAGE=disk}) e
 * idade mínima para um arquivo sem registro ser apagado pela limpeza de órfãos.
 */
@Validated
@ConfigurationProperties(prefix = "vsr.evidence")
public record EvidenceProperties(Path directory, @NotNull Duration orphanAge) {
}
