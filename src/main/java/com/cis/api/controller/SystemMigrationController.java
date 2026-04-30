package com.cis.api.controller;

import com.cis.api.config.SystemStateConfig;
import com.cis.api.migration.UserDataMigrationService;
import com.cis.api.migration.UserDataMigrationService.MigrationResult;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@Hidden
@Profile("!migrate & !test")
@RestController
@RequestMapping("/api/v1/system")
public class SystemMigrationController {

    private final ObjectProvider<UserDataMigrationService> migrationServiceProvider;
    private final ObjectProvider<SystemStateConfig>        systemStateProvider;

    public SystemMigrationController(
            ObjectProvider<UserDataMigrationService> migrationServiceProvider,
            ObjectProvider<SystemStateConfig>        systemStateProvider) {
        this.migrationServiceProvider = migrationServiceProvider;
        this.systemStateProvider      = systemStateProvider;
    }

    /**
     * Triggers the MySQL → MongoDB user migration.
     *
     * @param dryRun             when {@code true}, previews the migration without persisting data
     * @param cleanBeforeMigrate when {@code true}, wipes MongoDB users before migrating
     * @return 200 OK with the full {@link MigrationResult} summary, or 503 if
     *         the service is not available in the current context (test slice only)
     */
    @PostMapping("/migrate")
    public ResponseEntity<MigrationResult> triggerMigration(
            @RequestParam(defaultValue = "false") boolean dryRun,
            @RequestParam(defaultValue = "false") boolean cleanBeforeMigrate) {

        UserDataMigrationService migrationService = migrationServiceProvider.getIfAvailable();
        if (migrationService == null) {
            log.error("UserDataMigrationService is not available in the current context");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        log.info("Migration triggered by orchestrator — dryRun={}, cleanBeforeMigrate={}",
                dryRun, cleanBeforeMigrate);

        MigrationResult result = migrationService.migrateUsers(dryRun, cleanBeforeMigrate);

        log.info("Migration complete — {}", result.getSummary());
        return ResponseEntity.ok(result);
    }

    /**
     * Sets the {@code isV1Sunset} flag to {@code true}, activating the interceptor.
     * This call is idempotent; calling it multiple times has no additional effect.
     *
     * @return 200 OK with a plain confirmation message, or 503 if the state
     *         component is not available in the current context (test slice only)
     */
    @PostMapping("/sunset")
    public ResponseEntity<String> triggerSunset() {
        SystemStateConfig systemState = systemStateProvider.getIfAvailable();
        if (systemState == null) {
            log.error("SystemStateConfig is not available in the current context");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        log.warn("V1 Sunset triggered by orchestrator — write operations on /api/v1/** will now return 410 Gone");
        systemState.setV1Sunset(true);
        return ResponseEntity.ok("V1 API has been sunset successfully.");
    }
}