package com.cis.api.fallback;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FallbackWebMvcConfigTest {

    @Test
    void shouldRegisterInterceptor() {
        FallbackInterceptor interceptor = mock(FallbackInterceptor.class);
        FallbackWebMvcConfig config = new FallbackWebMvcConfig(interceptor);
        InterceptorRegistry registry = mock(InterceptorRegistry.class);
        InterceptorRegistration registration = mock(InterceptorRegistration.class);

        when(registry.addInterceptor(interceptor)).thenReturn(registration);
        when(registration.addPathPatterns(any(String[].class))).thenReturn(registration);

        config.addInterceptors(registry);

        verify(registry).addInterceptor(interceptor);
        verify(registration).addPathPatterns("/api/v1/**", "/api/v2/**");
    }
}
