package com.cis.api.migration;

import java.nio.file.Files;
import java.nio.file.Path;

public final class UserMigrationRunnerHarness {

    private UserMigrationRunnerHarness() {
    }

    public static void main(String[] args) throws Exception {
        String scenario = args[0];
        String[] runnerArgs = java.util.Arrays.copyOfRange(args, 1, args.length);
        String callFile = System.getProperty("harness.call.file");

        UserDataMigrationService migrationService = new UserDataMigrationService(null, null) {
            @Override
            public MigrationResult migrateUsers(boolean dryRun, boolean cleanBeforeMigrate) {
                if (callFile != null && !callFile.isBlank()) {
                    try {
                        Files.writeString(Path.of(callFile), dryRun + "," + cleanBeforeMigrate);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                MigrationResult result = new MigrationResult();
                switch (scenario) {
                    case "dry-run" -> result.totalFound = 2;
                    case "success" -> {
                        result.totalFound = 1;
                        result.successCount = 1;
                        result.finalCount = 1;
                    }
                    case "errors" -> {
                        result.totalFound = 1;
                        result.failCount = 1;
                        result.errors.add("MongoDB failure");
                    }
                    case "skipped" -> {
                        result.totalFound = 3;
                        result.skippedCount = 3;
                    }
                    case "empty" -> {
                    }
                    default -> throw new IllegalArgumentException("Unknown scenario: " + scenario);
                }
                return result;
            }
        };

        new UserMigrationRunner(migrationService).run(runnerArgs);
    }
}
