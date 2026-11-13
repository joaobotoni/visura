package com.visura.configuration;

import com.visura.properties.GoogleProperties;
import com.visura.token.GoogleToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleConfig {

    @Bean
    public GoogleToken googleToken(GoogleProperties properties) {
        return new GoogleToken(properties.clientId());
    }
}
