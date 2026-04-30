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

@ExtendWith(MockitoExtension.class)
@DisplayName("V1SunsetInterceptor")
class V1SunsetInterceptorTest {

    @RestController
    static class StubController {
        @GetMapping("/api/v1/users")
        String getV1() { return "ok"; }
        @PostMapping("/api/v1/users")
        String postV1() { return "ok"; }
        @PutMapping("/api/v1/users/{id}")
        String putV1(@PathVariable String id) { return "ok"; }
        @DeleteMapping("/api/v1/users/{id}")
        String deleteV1(@PathVariable String id) { return "ok"; }

        @GetMapping("/api/v2/users")
        String getV2() { return "ok"; }
        @PostMapping("/api/v2/users")
        String postV2() { return "ok"; }
        @PutMapping("/api/v2/users/{id}")
        String putV2(@PathVariable String id) { return "ok"; }
        @DeleteMapping("/api/v2/users/{id}")
        String deleteV2(@PathVariable String id) { return "ok"; }
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
    @DisplayName("when both flags are false")
    class AllInactive {
        @BeforeEach
        void noFlags() {
            when(systemStateConfig.isMigrationRunning()).thenReturn(false);
            when(systemStateConfig.isV1Sunset()).thenReturn(false);
        }

        @Test @DisplayName("v1 GET passes")
        void v1Get() throws Exception {
            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isOk())
                    .andExpect(header().doesNotExist(V1SunsetInterceptor.WARNING_HEADER_NAME));
        }
        @Test @DisplayName("v1 POST passes")
        void v1Post() throws Exception {
            mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isOk());
        }
        @Test @DisplayName("v2 GET passes")
        void v2Get() throws Exception {
            mockMvc.perform(get("/api/v2/users"))
                    .andExpect(status().isOk())
                    .andExpect(header().doesNotExist(V1SunsetInterceptor.WARNING_HEADER_NAME));
        }
        @Test @DisplayName("v2 POST passes")
        void v2Post() throws Exception {
            mockMvc.perform(post("/api/v2/users").contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("when migration is running")
    class MigrationRunning {
        @BeforeEach
        void setup() {
            when(systemStateConfig.isMigrationRunning()).thenReturn(true);
            when(systemStateConfig.isV1Sunset()).thenReturn(false);
        }

        @Test @DisplayName("v1 GET passes without warning")
        void v1Get() throws Exception {
            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isOk())
                    .andExpect(header().doesNotExist(V1SunsetInterceptor.WARNING_HEADER_NAME));
        }
        @Test @DisplayName("v1 POST returns 503 maintenance")
        void v1Post() throws Exception {
            mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.error").value(containsString("maintenance")));
        }
        @Test @DisplayName("v2 GET passes without warning")
        void v2Get() throws Exception {
            mockMvc.perform(get("/api/v2/users"))
                    .andExpect(status().isOk())
                    .andExpect(header().doesNotExist(V1SunsetInterceptor.WARNING_HEADER_NAME));
        }
        @Test @DisplayName("v2 POST returns 503 maintenance")
        void v2Post() throws Exception {
            mockMvc.perform(post("/api/v2/users").contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.error").value(containsString("maintenance")));
        }
        @Test @DisplayName("v2 PUT returns 503")
        void v2Put() throws Exception {
            mockMvc.perform(put("/api/v2/users/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isServiceUnavailable());
        }
        @Test @DisplayName("v2 DELETE returns 503")
        void v2Delete() throws Exception {
            mockMvc.perform(delete("/api/v2/users/1"))
                    .andExpect(status().isServiceUnavailable());
        }
    }

    @Nested
    @DisplayName("when only V1 sunset is active")
    class SunsetActive {
        @BeforeEach
        void setup() {
            when(systemStateConfig.isMigrationRunning()).thenReturn(false);
            when(systemStateConfig.isV1Sunset()).thenReturn(true);
        }

        @Test @DisplayName("v1 GET passes with Warning header")
        void v1Get() throws Exception {
            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isOk())
                    .andExpect(header().string(V1SunsetInterceptor.WARNING_HEADER_NAME,
                            V1SunsetInterceptor.WARNING_HEADER_VALUE));
        }
        @Test @DisplayName("v1 POST returns 410 Gone")
        void v1Post() throws Exception {
            mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isGone())
                    .andExpect(jsonPath("$.status").value(410));
        }
        @Test @DisplayName("v2 GET passes without warning")
        void v2Get() throws Exception {
            mockMvc.perform(get("/api/v2/users"))
                    .andExpect(status().isOk())
                    .andExpect(header().doesNotExist(V1SunsetInterceptor.WARNING_HEADER_NAME));
        }
        @Test @DisplayName("v2 POST passes normally")
        void v2Post() throws Exception {
            mockMvc.perform(post("/api/v2/users").contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isOk());
        }
        @Test @DisplayName("v2 PUT passes normally")
        void v2Put() throws Exception {
            mockMvc.perform(put("/api/v2/users/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isOk());
        }
        @Test @DisplayName("v2 DELETE passes normally")
        void v2Delete() throws Exception {
            mockMvc.perform(delete("/api/v2/users/1"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("when both migration and sunset are true (maintenance wins)")
    class BothFlagsTrue {
        @BeforeEach
        void setup() {
            when(systemStateConfig.isMigrationRunning()).thenReturn(true);
            when(systemStateConfig.isV1Sunset()).thenReturn(true);
        }

        @Test @DisplayName("v1 POST returns 503 (maintenance) not 410")
        void v1PostReturns503() throws Exception {
            mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.error").value(containsString("maintenance")));
        }
        @Test @DisplayName("v2 POST returns 503 (maintenance)")
        void v2PostReturns503() throws Exception {
            mockMvc.perform(post("/api/v2/users").contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isServiceUnavailable());
        }
        @Test @DisplayName("v1 GET passes without warning")
        void v1GetClean() throws Exception {
            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isOk())
                    .andExpect(header().doesNotExist(V1SunsetInterceptor.WARNING_HEADER_NAME));
        }
        @Test @DisplayName("v2 GET passes without warning")
        void v2GetClean() throws Exception {
            mockMvc.perform(get("/api/v2/users"))
                    .andExpect(status().isOk())
                    .andExpect(header().doesNotExist(V1SunsetInterceptor.WARNING_HEADER_NAME));
        }
    }
}