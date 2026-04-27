package com.cis.api.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Scanner;

@Slf4j
@Component
@Profile("migrate")
@RequiredArgsConstructor
public class UserMigrationRunner implements CommandLineRunner {

    private final UserDataMigrationService migrationService;

    @Override
    public void run(String... args) throws Exception {
        // Parse arguments
        boolean dryRun = Arrays.asList(args).contains("--dry-run");
        boolean clean = Arrays.asList(args).contains("--clean");
        boolean autoConfirm = Arrays.asList(args).contains("--yes");

        log.info("========================================");
        log.info("  USER MIGRATION TOOL - MySQL to MongoDB");
        log.info("========================================");

        log.info("Configuration:");
        log.info("  - Dry run: {}", dryRun);
        log.info("  - Clean before migrate: {}", clean);
        log.info("  - Source: MySQL (v1)");
        log.info("  - Target: MongoDB (v2)");

        if (!dryRun && !autoConfirm) {
            log.warn("\n  WARNING: This will modify data in MongoDB!");
            log.warn("   - Clean mode: {}", clean);
            log.warn("   - Data will be migrated from MySQL to MongoDB\n");

            System.out.print("Do you want to continue? (yes/no): ");
            Scanner scanner = new Scanner(System.in);
            String confirmation = scanner.nextLine();

            if (!"yes".equalsIgnoreCase(confirmation)) {
                log.info("Migration cancelled by user.");
                System.exit(0);
                return;
            }
        }

        // Execute migration
        log.info("\n Starting migration...\n");
        long startTime = System.currentTimeMillis();

        var result = migrationService.migrateUsers(dryRun, clean);

        long duration = System.currentTimeMillis() - startTime;

        // Print results
        printResults(result, duration, dryRun);

        // Exit with appropriate code
        if (dryRun) {
            log.info("Dry run completed. Exiting with code 0.");
            System.exit(0);
        } else if (result.hasErrors() || result.failCount > 0) {
            log.error("Migration failed with errors. Exiting with code 1.");
            System.exit(1);
        } else if (result.successCount == 0 && result.totalFound > 0) {
            log.warn("No new users were migrated (already exist?). Exiting with code 0.");
            System.exit(0);
        } else {
            log.info("Migration completed successfully. Exiting with code 0.");
            System.exit(0);
        }
    }

    private void printResults(UserDataMigrationService.MigrationResult result, long duration, boolean dryRun) {
        log.info("\n========================================");
        log.info("         MIGRATION RESULTS");
        log.info("========================================");
        log.info(" Statistics:");
        log.info("   - Total users found in MySQL: {}", result.totalFound);
        log.info("   - Successfully migrated: {}", result.successCount);
        log.info("   - Failed migrations: {}", result.failCount);
        log.info("   - Skipped (already exist): {}", result.skippedCount);
        log.info("   - Cleaned from MongoDB: {}", result.cleanedCount);
        log.info("   - Final users in MongoDB: {}", result.finalCount);
        log.info("  Duration: {} ms ({} seconds)", duration, duration / 1000);

        if (!result.errors.isEmpty()) {
            log.error("\n❌ Errors encountered:");
            for (String error : result.errors) {
                log.error("   - {}", error);
            }
        }

        if (dryRun) {
            log.info("\n Dry run completed - no data was modified");
        } else if (result.failCount == 0 && result.successCount > 0) {
            log.info("\n Migration completed successfully!");
        } else if (result.skippedCount > 0 && result.successCount == 0 && result.totalFound > 0) {
            log.warn("\n Migration skipped - users already exist in MongoDB");
        } else if (result.successCount > 0) {
            log.warn("\n Migration completed with warnings.");
        } else if (result.totalFound == 0) {
            log.info("\n No users found in MySQL to migrate.");
        } else {
            log.error("\n Migration failed!");
        }

        log.info("\n Verification:");
        log.info("   Run: curl http://localhost:8080/api/v2/users");
    }
}