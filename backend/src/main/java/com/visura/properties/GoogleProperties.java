package com.visura.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Client ID (do tipo "Web application") do projeto no Google Cloud, o mesmo que o app usa para pedir o ID token.
 * Vazio desliga o login com Google.
 */
@ConfigurationProperties(prefix = "vsr.google")
public record GoogleProperties(String clientId) {
}
