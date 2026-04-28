package com.cis.api.fallback;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers the fallback interceptor for API endpoints.
 */
@Configuration
@ConditionalOnProperty(prefix = "app.fallback", name = "enabled", havingValue = "true")
public class FallbackWebMvcConfig implements WebMvcConfigurer {

    private final FallbackInterceptor fallbackInterceptor;

    public FallbackWebMvcConfig(FallbackInterceptor fallbackInterceptor) {
        this.fallbackInterceptor = fallbackInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(fallbackInterceptor)
                .addPathPatterns("/api/v1/**", "/api/v2/**");
    }
}
