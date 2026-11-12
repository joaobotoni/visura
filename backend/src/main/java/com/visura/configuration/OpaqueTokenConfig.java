package com.visura.configuration;

import com.visura.properties.RefreshTokenProperties;
import com.visura.token.OpaqueToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpaqueTokenConfig {

    @Bean
    public OpaqueToken opaqueToken(RefreshTokenProperties properties) {
        return new OpaqueToken(properties.secretKey());
    }
}
