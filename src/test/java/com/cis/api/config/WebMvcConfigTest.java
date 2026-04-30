package com.cis.api.config;

import com.cis.api.interceptor.V1SunsetInterceptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link WebMvcConfig}.
 *
 * We construct {@link WebMvcConfig} directly (no Spring context) and supply
 * hand-crafted {@link ObjectProvider} doubles so we can test both branches:
 * interceptor present (full application context) and interceptor absent
 * (@WebMvcTest slice context).
 */
@DisplayName("WebMvcConfig")
class WebMvcConfigTest {

    @SuppressWarnings("unchecked")
    private static ObjectProvider<V1SunsetInterceptor> presentProvider(V1SunsetInterceptor bean) {
        ObjectProvider<V1SunsetInterceptor> provider = mock(ObjectProvider.class);
        doAnswer(inv -> {
            ((Consumer<V1SunsetInterceptor>) inv.getArgument(0)).accept(bean);
            return null;
        }).when(provider).ifAvailable(any());
        return provider;
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<V1SunsetInterceptor> absentProvider() {
        ObjectProvider<V1SunsetInterceptor> provider = mock(ObjectProvider.class);
        doNothing().when(provider).ifAvailable(any()); // no-op
        return provider;
    }

    @Nested
    @DisplayName("when V1SunsetInterceptor bean IS available (full context)")
    class BeanPresent {

        @Test
        @DisplayName("registers interceptor for /api/v1/** and excludes /api/v1/system/**")
        void registersInterceptorWithCorrectPaths() {
            V1SunsetInterceptor     interceptor  = mock(V1SunsetInterceptor.class);
            InterceptorRegistry     registry     = mock(InterceptorRegistry.class);
            InterceptorRegistration registration = mock(InterceptorRegistration.class);

            when(registry.addInterceptor(interceptor)).thenReturn(registration);
            when(registration.addPathPatterns(any(String.class))).thenReturn(registration);
            when(registration.excludePathPatterns(any(String.class))).thenReturn(registration);

            new WebMvcConfig(presentProvider(interceptor)).addInterceptors(registry);

            verify(registry).addInterceptor(interceptor);
            verify(registration).addPathPatterns("/api/v1/**");
            verify(registration).excludePathPatterns("/api/v1/system/**");
        }

        @Test
        @DisplayName("does not register any other interceptors")
        void doesNotRegisterExtraInterceptors() {
            V1SunsetInterceptor     interceptor  = mock(V1SunsetInterceptor.class);
            InterceptorRegistry     registry     = mock(InterceptorRegistry.class);
            InterceptorRegistration registration = mock(InterceptorRegistration.class);

            when(registry.addInterceptor(interceptor)).thenReturn(registration);
            when(registration.addPathPatterns(any(String.class))).thenReturn(registration);
            when(registration.excludePathPatterns(any(String.class))).thenReturn(registration);

            new WebMvcConfig(presentProvider(interceptor)).addInterceptors(registry);

            verify(registry, times(1)).addInterceptor(any());
        }
    }

    @Nested
    @DisplayName("when V1SunsetInterceptor bean is ABSENT (@WebMvcTest slice)")
    class BeanAbsent {

        @Test
        @DisplayName("addInterceptors is a no-op — registry is never touched")
        void addInterceptors_isNoOp_registryUntouched() {
            InterceptorRegistry registry = mock(InterceptorRegistry.class);

            new WebMvcConfig(absentProvider()).addInterceptors(registry);

            verifyNoInteractions(registry);
        }

        @Test
        @DisplayName("addInterceptors does not throw")
        void addInterceptors_doesNotThrow() {
            InterceptorRegistry registry = mock(InterceptorRegistry.class);
            new WebMvcConfig(absentProvider()).addInterceptors(registry);
        }
    }
}