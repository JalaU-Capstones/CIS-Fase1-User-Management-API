package com.cis.api.migration;

import com.cis.api.model.User;
import com.cis.api.repository.UserRepository;
import com.cis.api.repository.MongoPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDataMigrationService {

    private final UserRepository mysqlUserRepository;      // MySQL/MyBatis (v1)
    private final MongoPersistencePort mongoPersistencePort; // MongoDB (v2)

    @Transactional
    public MigrationResult migrateUsers(boolean dryRun, boolean cleanBeforeMigrate) {
        log.info("Starting user migration from MySQL to MongoDB (Phase 3)");
        MigrationResult result = new MigrationResult();

        try {
            // Clean MongoDB if requested
            if (cleanBeforeMigrate && !dryRun) {
                log.info("Cleaning existing users from MongoDB...");
                var existingUsers = mongoPersistencePort.findAll();
                for (User user : existingUsers) {
                    mongoPersistencePort.deleteUserAndRelatedData(user.getId());
                    result.cleanedCount++;
                }
                log.info("Cleaned {} users from MongoDB", result.cleanedCount);
            }

            // Get all users from MySQL
            List<User> mysqlUsers = mysqlUserRepository.findAll();
            result.totalFound = mysqlUsers.size();
            log.info("Found {} users in MySQL", result.totalFound);

            if (dryRun) {
                log.info("DRY RUN - Would migrate {} users", result.totalFound);
                mysqlUsers.forEach(user ->
                        log.info("  Would migrate: id={}, login={}", user.getId(), user.getLogin())
                );
                return result;
            }

            // Migrate each user
            int successCount = 0;
            int failCount = 0;
            int skippedCount = 0;
            List<String> errors = new ArrayList<>();

            for (User mysqlUser : mysqlUsers) {
                try {
                    // Check if already exists in MongoDB
                    if (!mongoPersistencePort.existsByLogin(mysqlUser.getLogin())) {
                        // Generate new UUID if needed, or keep existing
                        if (mysqlUser.getId() == null) {
                            mysqlUser.setId(UUID.randomUUID());
                        }

                        // Save to MongoDB
                        mongoPersistencePort.save(mysqlUser);
                        successCount++;
                        log.debug("Migrated user: {}", mysqlUser.getLogin());
                    } else {
                        skippedCount++;
                        log.warn("User {} already exists in MongoDB, skipping", mysqlUser.getLogin());
                    }
                } catch (Exception e) {
                    failCount++;
                    errors.add(String.format("User %s: %s", mysqlUser.getLogin(), e.getMessage()));
                    log.error("Failed to migrate user {}: {}", mysqlUser.getLogin(), e.getMessage(), e);
                }
            }

            result.successCount = successCount;
            result.failCount = failCount;
            result.skippedCount = skippedCount;
            result.errors = errors;

            // Verification
            List<User> finalUsers = mongoPersistencePort.findAll();
            result.finalCount = finalUsers.size();

            log.info("Migration completed: {} succeeded, {} failed, {} skipped",
                    successCount, failCount, skippedCount);
            log.info("Verification: {} users in MongoDB", result.finalCount);

        } catch (Exception e) {
            log.error("Migration failed: ", e);
            result.errors.add(e.getMessage());
        }

        return result;
    }

    public static class MigrationResult {
        public int totalFound = 0;
        public int successCount = 0;
        public int failCount = 0;
        public int skippedCount = 0;
        public int cleanedCount = 0;
        public int finalCount = 0;
        public List<String> errors = new ArrayList<>();

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public String getSummary() {
            return String.format(
                    "Migration Summary: Total=%d, Success=%d, Failed=%d, Skipped=%d, Cleaned=%d, Final=%d",
                    totalFound, successCount, failCount, skippedCount, cleanedCount, finalCount
            );
        }
    }
}