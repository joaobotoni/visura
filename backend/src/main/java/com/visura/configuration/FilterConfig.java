package com.visura.configuration;

import com.visura.filter.AuthenticationFilter;
import com.visura.filter.ExceptionFilter;
import com.visura.filter.RateLimitFilter;
import com.visura.filter.RequestBodySizeLimitFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<ExceptionFilter> exceptionFilterRegistration(ExceptionFilter filter) {
        return disabled(filter);
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RateLimitFilter filter) {
        return disabled(filter);
    }

    @Bean
    public FilterRegistrationBean<AuthenticationFilter> authenticationFilterRegistration(AuthenticationFilter filter) {
        return disabled(filter);
    }

    @Bean
    public FilterRegistrationBean<RequestBodySizeLimitFilter> bodySizeFilterRegistration(RequestBodySizeLimitFilter filter) {
        return disabled(filter);
    }

    private static <T extends Filter> FilterRegistrationBean<T> disabled(T filter) {
        FilterRegistrationBean<T> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
