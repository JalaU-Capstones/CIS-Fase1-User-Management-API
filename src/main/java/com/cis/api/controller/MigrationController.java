package com.cis.api.controller;

import com.cis.api.migration.UserDataMigrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Migration", description = "Data migration operations from MySQL to MongoDB")
@RestController
@RequestMapping("/api/migration")
@RequiredArgsConstructor
public class MigrationController {

    private final UserDataMigrationService migrationService;

    @Operation(summary = "Migrate users from MySQL to MongoDB")
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDataMigrationService.MigrationResult> migrateUsers(
            @RequestParam(defaultValue = "false") boolean dryRun,
            @RequestParam(defaultValue = "false") boolean clean) {

        var result = migrationService.migrateUsers(dryRun, clean);

        if (result.hasErrors() && !dryRun) {
            return ResponseEntity.status(500).body(result);
        }

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get migration status and information")
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getMigrationStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("phase", "Phase 3 - MongoDB Migration");
        status.put("source", "MySQL (API v1)");
        status.put("target", "MongoDB (API v2)");
        status.put("status", "ready");
        status.put("endpoint", "POST /api/migration/users?dryRun=true&clean=false");
        return ResponseEntity.ok(status);
    }
}