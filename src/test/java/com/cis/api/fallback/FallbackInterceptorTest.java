package com.cis.api.fallback;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class FallbackInterceptorTest {

    @AfterEach
    void cleanup() {
        RequestVersionContext.clear();
    }

    @Test
    void allowsGetEvenWhenFallbackActive() throws Exception {
        DatabaseFallbackService fallbackService = mock(DatabaseFallbackService.class);
        given(fallbackService.isFallbackActive()).willReturn(true);
        // for assertAtLeastOneDatabaseUp
        given(fallbackService.getActiveDatabase("v1")).willReturn(DatabaseFallbackService.DB_MONGO);

        FallbackInterceptor interceptor = new FallbackInterceptor(fallbackService);

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/users");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        boolean proceed = interceptor.preHandle(req, resp, new Object());

        assertThat(proceed).isTrue();
        assertThat(RequestVersionContext.getOriginalVersion()).isEqualTo("v1");
    }

    @Test
    void blocksWriteWhenFallbackActive() throws Exception {
        DatabaseFallbackService fallbackService = mock(DatabaseFallbackService.class);
        given(fallbackService.isFallbackActive()).willReturn(true);
        given(fallbackService.getActiveDatabase("v2")).willReturn(DatabaseFallbackService.DB_MYSQL);

        FallbackInterceptor interceptor = new FallbackInterceptor(fallbackService);

        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v2/users");
        MockHttpServletResponse resp = new MockHttpServletResponse();

        boolean proceed = interceptor.preHandle(req, resp, new Object());

        assertThat(proceed).isFalse();
        assertThat(resp.getStatus()).isEqualTo(503);
        assertThat(resp.getContentAsString()).contains("planned maintenance");
    }
}

