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

    @PostMapping("/maintenance/start")
    public ResponseEntity<String> startMaintenance() {
        SystemStateConfig systemState = systemStateProvider.getIfAvailable();
        if (systemState == null) {
            log.error("SystemStateConfig is not available in the current context");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        log.warn("Maintenance mode STARTED — write operations on /api/v1/** will return 503");
        systemState.setMigrationRunning(true);
        return ResponseEntity.ok("Maintenance mode activated.");
    }

    @PostMapping("/maintenance/stop")
    public ResponseEntity<String> stopMaintenance() {
        SystemStateConfig systemState = systemStateProvider.getIfAvailable();
        if (systemState == null) {
            log.error("SystemStateConfig is not available in the current context");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        log.warn("Maintenance mode STOPPED — write operations on /api/v1/** are allowed again");
        systemState.setMigrationRunning(false);
        return ResponseEntity.ok("Maintenance mode deactivated.");
    }

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

    @PostMapping("/sunset")
    public ResponseEntity<String> triggerSunset() {
        SystemStateConfig systemState = systemStateProvider.getIfAvailable();
        if (systemState == null) {
            log.error("SystemStateConfig is not available in the current context");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        log.warn("V1 Sunset triggered — stopping maintenance and activating sunset");
        systemState.setMigrationRunning(false);
        systemState.setV1Sunset(true);
        return ResponseEntity.ok("V1 API has been sunset successfully.");
    }
}