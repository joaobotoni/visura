package com.visura.support;

import com.visura.configuration.ClockConfig;
import com.visura.configuration.DenialsConfig;
import com.visura.configuration.FilterConfig;
import com.visura.configuration.JwtConfig;
import com.visura.configuration.SecurityConfig;
import com.visura.properties.JwtProperties;
import com.visura.properties.RateLimitProperties;
import com.visura.properties.RequestProperties;
import com.visura.properties.VersionProperties;
import com.visura.configuration.RateLimitConfig;
import com.visura.service.TokenService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@EnableConfigurationProperties({JwtProperties.class, RateLimitProperties.class, RequestProperties.class, VersionProperties.class})
@Import({ClockConfig.class, SecurityConfig.class, DenialsConfig.class, FilterConfig.class, RateLimitConfig.class, JwtConfig.class, TokenService.class})
class WebSecurityTestConfiguration {
}
