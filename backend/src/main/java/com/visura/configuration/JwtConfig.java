package com.visura.configuration;

import com.visura.properties.JwtProperties;
import com.visura.token.JwtToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class JwtConfig {

    @Bean
    public JwtToken jwtToken(JwtProperties properties, Clock clock) {
        return new JwtToken(properties.secretKey(), properties.issuer(), properties.expirationTime(), clock);
    }
}
