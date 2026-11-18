package com.visura.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bucket das evidências com {@code EVIDENCE_STORAGE=bucket}: nome, região, endereço (vazio para a AWS; o endpoint da
 * conta no Cloudflare R2 ou o do MinIO), prefixo das chaves e endereçamento por caminho (necessário no MinIO). As
 * credenciais vêm da cadeia padrão da AWS ({@code AWS_ACCESS_KEY_ID} e {@code AWS_SECRET_ACCESS_KEY}).
 */
@ConfigurationProperties(prefix = "vsr.evidence.bucket")
public record BucketProperties(String name, String region, String endpoint, String prefix, boolean pathStyle) {
}
