package com.cis.api.config;

import com.cis.api.interceptor.V1SunsetInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile("!migrate & !test")
public class WebMvcConfig implements WebMvcConfigurer {

    private final ObjectProvider<V1SunsetInterceptor> interceptorProvider;

    public WebMvcConfig(ObjectProvider<V1SunsetInterceptor> interceptorProvider) {
        this.interceptorProvider = interceptorProvider;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        interceptorProvider.ifAvailable(interceptor ->
                registry.addInterceptor(interceptor)
                        .addPathPatterns("/api/v1/**", "/api/v2/**")
        );
    }
}