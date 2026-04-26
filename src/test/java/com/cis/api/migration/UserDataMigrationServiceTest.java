package com.cis.api.migration;

import com.cis.api.model.User;
import com.cis.api.repository.MongoPersistencePort;
import com.cis.api.repository.UserRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("MongoDB not available in CI/CD pipeline - Run locally only with MongoDB running")
class UserDataMigrationServiceTest {

    @Autowired
    private UserDataMigrationService migrationService;

    @Autowired
    private UserRepository mysqlUserRepository;

    @Autowired
    private MongoPersistencePort mongoPersistencePort;

    @Test
    void testDryRunMigration() {
        // Given - Create test user in MySQL
        User user = createTestUserInMysql();

        // When
        var result = migrationService.migrateUsers(true, false);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.totalFound).isGreaterThan(0);
        assertThat(result.successCount).isZero(); // Dry run doesn't save
        assertThat(result.errors).isEmpty();

        // Clean up
        mysqlUserRepository.deleteById(user.getId());
    }

    @Test
    void testFullMigration() {
        // Given - Create test user in MySQL
        User user = createTestUserInMysql();

        // When
        var result = migrationService.migrateUsers(false, true);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.successCount).isEqualTo(result.totalFound);
        assertThat(result.failCount).isZero();
        assertThat(mongoPersistencePort.findAll()).hasSizeGreaterThanOrEqualTo(result.finalCount);

        // Clean up
        mysqlUserRepository.deleteById(user.getId());

        // Optional: Clean MongoDB test data
        if (mongoPersistencePort.existsByLogin(user.getLogin())) {
            var mongoUser = mongoPersistencePort.findByLogin(user.getLogin()).orElse(null);
            if (mongoUser != null) {
                mongoPersistencePort.deleteUserAndRelatedData(mongoUser.getId());
            }
        }
    }

    private User createTestUserInMysql() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Test User");
        // Ensure login length is <= 20 characters
        String timestamp = String.valueOf(System.currentTimeMillis());
        String suffix = timestamp.substring(timestamp.length() - 6); // Last 6 digits
        user.setLogin("test_" + suffix);
        user.setPassword("password123");
        return mysqlUserRepository.save(user);
    }
}