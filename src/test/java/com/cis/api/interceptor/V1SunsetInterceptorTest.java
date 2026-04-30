package com.cis.api.interceptor;

import com.cis.api.config.SystemStateConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link V1SunsetInterceptor} using
 * {@link MockMvcBuilders#standaloneSetup} so no Spring context is loaded and
 * the security filter chain is bypassed — we are testing only interceptor logic.
 *
 * <p>A minimal {@link StubController} is wired in as the target endpoint;
 * it always returns 200 OK so any non-200 response is caused by the interceptor.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("V1SunsetInterceptor")
class V1SunsetInterceptorTest {

    @RestController
    static class StubController {
        @GetMapping("/api/v1/users")
        String get() { return "ok"; }

        @PostMapping("/api/v1/users")
        String post() { return "ok"; }

        @PutMapping("/api/v1/users/{id}")
        String put(@PathVariable String id) { return "ok"; }

        @DeleteMapping("/api/v1/users/{id}")
        String delete(@PathVariable String id) { return "ok"; }
    }

    @Mock
    private SystemStateConfig systemStateConfig;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        V1SunsetInterceptor interceptor = new V1SunsetInterceptor(systemStateConfig, new ObjectMapper());
        mockMvc = MockMvcBuilders
                .standaloneSetup(new StubController())
                .addInterceptors(interceptor)
                .build();
    }

    @Nested
    @DisplayName("when sunset is NOT active")
    class SunsetInactive {

        @BeforeEach
        void sunsetOff() {
            when(systemStateConfig.isV1Sunset()).thenReturn(false);
        }

        @Test
        @DisplayName("GET passes through with no Warning header")
        void get_passesWithoutWarningHeader() throws Exception {
            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isOk())
                    .andExpect(header().doesNotExist(V1SunsetInterceptor.WARNING_HEADER_NAME));
        }

        @Test
        @DisplayName("POST passes through normally")
        void post_passesThrough() throws Exception {
            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("PUT passes through normally")
        void put_passesThrough() throws Exception {
            mockMvc.perform(put("/api/v1/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("DELETE passes through normally")
        void delete_passesThrough() throws Exception {
            mockMvc.perform(delete("/api/v1/users/1"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("when sunset IS active")
    class SunsetActive {

        @BeforeEach
        void sunsetOn() {
            when(systemStateConfig.isV1Sunset()).thenReturn(true);
        }

        @Test
        @DisplayName("GET passes through and carries the RFC-7234 Warning header")
        void get_passesWithWarningHeader() throws Exception {
            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isOk())
                    .andExpect(header().exists(V1SunsetInterceptor.WARNING_HEADER_NAME))
                    .andExpect(header().string(V1SunsetInterceptor.WARNING_HEADER_NAME,
                            containsString("API v1 is deprecated")))
                    .andExpect(header().string(V1SunsetInterceptor.WARNING_HEADER_NAME,
                            containsString("/api/v2/")));
        }

        @Test
        @DisplayName("GET Warning header contains the full expected value")
        void get_warningHeaderContainsFullValue() throws Exception {
            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(header().string(V1SunsetInterceptor.WARNING_HEADER_NAME,
                            V1SunsetInterceptor.WARNING_HEADER_VALUE));
        }

        @Test
        @DisplayName("POST is blocked with 410 Gone")
        void post_returns410Gone() throws Exception {
            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isGone());
        }

        @Test
        @DisplayName("POST 410 body contains status, error, and message fields")
        void post_blockedBody_containsJsonFields() throws Exception {
            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isGone())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(410))
                    .andExpect(jsonPath("$.error").value("Gone"))
                    .andExpect(jsonPath("$.message").value(containsString("/api/v2/")));
        }


        @Test
        @DisplayName("PUT is blocked with 410 Gone")
        void put_returns410Gone() throws Exception {
            mockMvc.perform(put("/api/v1/users/42")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isGone());
        }

        @Test
        @DisplayName("PUT 410 body is valid JSON")
        void put_blockedBody_isJson() throws Exception {
            mockMvc.perform(put("/api/v1/users/42")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(410));
        }

        @Test
        @DisplayName("DELETE is blocked with 410 Gone")
        void delete_returns410Gone() throws Exception {
            mockMvc.perform(delete("/api/v1/users/42"))
                    .andExpect(status().isGone());
        }

        @Test
        @DisplayName("DELETE 410 body is valid JSON")
        void delete_blockedBody_isJson() throws Exception {
            mockMvc.perform(delete("/api/v1/users/42"))
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error").value("Gone"));
        }
    }
}