package com.cis.api.controller;

import com.cis.api.config.SystemStateConfig;
import com.cis.api.migration.UserDataMigrationService;
import com.cis.api.migration.UserDataMigrationService.MigrationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link SystemMigrationController}.
 *
 * Uses {@link MockMvcBuilders#standaloneSetup} to isolate from Spring Security.
 * The controller is constructed directly with hand-crafted {@link ObjectProvider}
 * doubles, mirroring the approach used in {@code WebMvcConfigTest}.
 */
@DisplayName("SystemMigrationController")
class SystemMigrationControllerTest {


    @SuppressWarnings("unchecked")
    private static <T> ObjectProvider<T> providerOf(T bean) {
        ObjectProvider<T> p = mock(ObjectProvider.class);
        when(p.getIfAvailable()).thenReturn(bean);
        return p;
    }

    @SuppressWarnings("unchecked")
    private static <T> ObjectProvider<T> emptyProvider() {
        ObjectProvider<T> p = mock(ObjectProvider.class);
        when(p.getIfAvailable()).thenReturn(null);
        return p;
    }


    @Nested
    @DisplayName("POST /api/v1/system/migrate")
    class MigrateEndpoint {

        @Test
        @DisplayName("default params — delegates migrateUsers(false, false) and returns 200")
        void defaultParams_delegates_returns200() throws Exception {
            UserDataMigrationService service = mock(UserDataMigrationService.class);
            MigrationResult result = buildResult(3, 3, 0, 0);
            when(service.migrateUsers(false, false)).thenReturn(result);

            mockMvc(service, mock(SystemStateConfig.class))
                    .perform(post("/api/v1/system/migrate"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.totalFound").value(3))
                    .andExpect(jsonPath("$.successCount").value(3))
                    .andExpect(jsonPath("$.failCount").value(0))
                    .andExpect(jsonPath("$.skippedCount").value(0));

            verify(service).migrateUsers(false, false);
            verifyNoMoreInteractions(service);
        }

        @Test
        @DisplayName("dryRun=true — forwarded correctly")
        void dryRun_forwarded() throws Exception {
            UserDataMigrationService service = mock(UserDataMigrationService.class);
            when(service.migrateUsers(true, false)).thenReturn(new MigrationResult());

            mockMvc(service, mock(SystemStateConfig.class))
                    .perform(post("/api/v1/system/migrate").param("dryRun", "true"))
                    .andExpect(status().isOk());

            verify(service).migrateUsers(true, false);
        }

        @Test
        @DisplayName("cleanBeforeMigrate=true — forwarded correctly and cleanedCount in response")
        void cleanBeforeMigrate_forwarded() throws Exception {
            UserDataMigrationService service = mock(UserDataMigrationService.class);
            MigrationResult result = buildResult(5, 5, 0, 0);
            result.cleanedCount = 2;
            when(service.migrateUsers(false, true)).thenReturn(result);

            mockMvc(service, mock(SystemStateConfig.class))
                    .perform(post("/api/v1/system/migrate").param("cleanBeforeMigrate", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cleanedCount").value(2));

            verify(service).migrateUsers(false, true);
        }

        @Test
        @DisplayName("both params true — forwarded correctly")
        void bothParamsTrue_forwarded() throws Exception {
            UserDataMigrationService service = mock(UserDataMigrationService.class);
            when(service.migrateUsers(true, true)).thenReturn(new MigrationResult());

            mockMvc(service, mock(SystemStateConfig.class))
                    .perform(post("/api/v1/system/migrate")
                            .param("dryRun", "true")
                            .param("cleanBeforeMigrate", "true"))
                    .andExpect(status().isOk());

            verify(service).migrateUsers(true, true);
        }

        @Test
        @DisplayName("migration with failures — failCount serialised in response")
        void failCount_inResponse() throws Exception {
            UserDataMigrationService service = mock(UserDataMigrationService.class);
            when(service.migrateUsers(false, false)).thenReturn(buildResult(5, 3, 2, 0));

            mockMvc(service, mock(SystemStateConfig.class))
                    .perform(post("/api/v1/system/migrate"))
                    .andExpect(jsonPath("$.failCount").value(2))
                    .andExpect(jsonPath("$.successCount").value(3));
        }

        @Test
        @DisplayName("migration with skips — skippedCount serialised in response")
        void skippedCount_inResponse() throws Exception {
            UserDataMigrationService service = mock(UserDataMigrationService.class);
            when(service.migrateUsers(false, false)).thenReturn(buildResult(5, 3, 0, 2));

            mockMvc(service, mock(SystemStateConfig.class))
                    .perform(post("/api/v1/system/migrate"))
                    .andExpect(jsonPath("$.skippedCount").value(2));
        }

        @Test
        @DisplayName("service unavailable (test slice) — returns 503")
        void serviceUnavailable_returns503() throws Exception {
            mockMvcWithAbsentBeans()
                    .perform(post("/api/v1/system/migrate"))
                    .andExpect(status().isServiceUnavailable());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/system/sunset")
    class SunsetEndpoint {

        @Test
        @DisplayName("returns 200 OK")
        void returns200() throws Exception {
            mockMvc(mock(UserDataMigrationService.class), mock(SystemStateConfig.class))
                    .perform(post("/api/v1/system/sunset"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("sets isV1Sunset flag to true")
        void setsSunsetFlagTrue() throws Exception {
            SystemStateConfig state = mock(SystemStateConfig.class);

            mockMvc(mock(UserDataMigrationService.class), state)
                    .perform(post("/api/v1/system/sunset"))
                    .andExpect(status().isOk());

            verify(state).setV1Sunset(true);
            verifyNoMoreInteractions(state);
        }

        @Test
        @DisplayName("response body confirms the action")
        void responseBody_confirmsAction() throws Exception {
            mockMvc(mock(UserDataMigrationService.class), mock(SystemStateConfig.class))
                    .perform(post("/api/v1/system/sunset"))
                    .andExpect(content().string(containsString("sunset")));
        }

        @Test
        @DisplayName("sunset never touches the migration service")
        void doesNotTouchMigrationService() throws Exception {
            UserDataMigrationService service = mock(UserDataMigrationService.class);

            mockMvc(service, mock(SystemStateConfig.class))
                    .perform(post("/api/v1/system/sunset"))
                    .andExpect(status().isOk());

            verifyNoInteractions(service);
        }

        @Test
        @DisplayName("idempotent — second call still delegates setV1Sunset(true)")
        void idempotent_secondCallStillSetsFlag() throws Exception {
            SystemStateConfig state = mock(SystemStateConfig.class);
            MockMvc mvc = mockMvc(mock(UserDataMigrationService.class), state);

            mvc.perform(post("/api/v1/system/sunset")).andExpect(status().isOk());
            mvc.perform(post("/api/v1/system/sunset")).andExpect(status().isOk());

            verify(state, times(2)).setV1Sunset(true);
        }

        @Test
        @DisplayName("state unavailable (test slice) — returns 503")
        void stateUnavailable_returns503() throws Exception {
            mockMvcWithAbsentBeans()
                    .perform(post("/api/v1/system/sunset"))
                    .andExpect(status().isServiceUnavailable());
        }
    }

    private MockMvc mockMvc(UserDataMigrationService service, SystemStateConfig state) {
        return MockMvcBuilders
                .standaloneSetup(new SystemMigrationController(providerOf(service), providerOf(state)))
                .build();
    }

    private MockMvc mockMvcWithAbsentBeans() {
        return MockMvcBuilders
                .standaloneSetup(new SystemMigrationController(emptyProvider(), emptyProvider()))
                .build();
    }

    private MigrationResult buildResult(int total, int success, int fail, int skipped) {
        MigrationResult r = new MigrationResult();
        r.totalFound   = total;
        r.successCount = success;
        r.failCount    = fail;
        r.skippedCount = skipped;
        return r;
    }
}