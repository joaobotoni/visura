package com.visura.configuration;

import com.visura.properties.RateLimitProperties;
import com.visura.ratelimit.RateLimitPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    @Bean
    public RateLimitPolicy rateLimitPolicy(RateLimitProperties properties) {
        return RateLimitPolicy.from(properties);
    }
}
