package com.cis.api.migration;

import com.cis.api.model.MongoUser;
import com.cis.api.model.User;
import com.cis.api.repository.MongoUserSpringRepository;
import com.cis.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDataMigrationService {

    private final UserRepository mysqlUserRepository;
    private final MongoUserSpringRepository mongoUserRepository;

    /**
     * Migrates users from MySQL to MongoDB.
     *
     * @param dryRun if true, only previews what would be migrated without saving any data
     * @param cleanBeforeMigrate if true, deletes all existing users from MongoDB before migration
     * @return MigrationResult containing statistics about the migration operation
     */
    @Transactional
    public MigrationResult migrateUsers(boolean dryRun, boolean cleanBeforeMigrate) {
        log.info("Starting user migration from MySQL to MongoDB (Phase 3)");
        MigrationResult result = new MigrationResult();

        try {
            // 1. Clean MongoDB if requested
            if (cleanBeforeMigrate && !dryRun) {
                log.info("Cleaning existing users from MongoDB...");
                long count = mongoUserRepository.count();
                mongoUserRepository.deleteAll();
                result.cleanedCount = (int) count;
                log.info("Cleaned {} users from MongoDB", count);
            }

            // 2. Get all users from MySQL
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

            // 3. Migrate each user
            int successCount = 0;
            int failCount = 0;
            int skippedCount = 0;
            List<String> errors = new ArrayList<>();

            for (User mysqlUser : mysqlUsers) {
                try {
                    // Check if already exists in MongoDB by ID or login
                    boolean existsById = mongoUserRepository.existsById(mysqlUser.getId().toString());
                    boolean existsByLogin = mongoUserRepository.existsByLogin(mysqlUser.getLogin());

                    if (!existsById && !existsByLogin) {
                        MongoUser mongoUser = transformToMongoUser(mysqlUser);
                        mongoUserRepository.save(mongoUser);
                        successCount++;
                        log.debug("Migrated user: {} (id: {})", mysqlUser.getLogin(), mysqlUser.getId());
                    } else {
                        skippedCount++;
                        log.warn("User {} (id: {}) already exists in MongoDB, skipping",
                                mysqlUser.getLogin(), mysqlUser.getId());
                    }
                } catch (Exception e) {
                    failCount++;
                    errors.add(String.format("User %s (id: %s): %s",
                            mysqlUser.getLogin(), mysqlUser.getId(), e.getMessage()));
                    log.error("Failed to migrate user {}: {}", mysqlUser.getLogin(), e.getMessage(), e);
                }
            }

            result.successCount = successCount;
            result.failCount = failCount;
            result.skippedCount = skippedCount;
            result.errors = errors;

            // 4. Verification
            long finalCount = mongoUserRepository.count();
            result.finalCount = (int) finalCount;

            log.info("Migration completed: {} succeeded, {} failed, {} skipped",
                    successCount, failCount, skippedCount);
            log.info("Verification: {} users in MongoDB", result.finalCount);

        } catch (Exception e) {
            log.error("Migration failed: ", e);
            result.errors.add(e.getMessage());
        }

        return result;
    }

    /**
     * Transforms a MySQL User entity to a MongoDB MongoUser document.
     * Ensures field names match exactly what the C# Phase 2 API expects.
     *
     * @param mysqlUser the MySQL user entity to transform
     * @return a MongoUser document ready for MongoDB persistence
     */
    private MongoUser transformToMongoUser(User mysqlUser) {
        MongoUser mongoUser = new MongoUser();
        mongoUser.setId(mysqlUser.getId().toString());
        mongoUser.setName(mysqlUser.getName());
        mongoUser.setLogin(mysqlUser.getLogin());
        mongoUser.setPassword(mysqlUser.getPassword());
        return mongoUser;
    }

    /**
     * Result container for migration operations.
     * Contains statistics about the migration run.
     */
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