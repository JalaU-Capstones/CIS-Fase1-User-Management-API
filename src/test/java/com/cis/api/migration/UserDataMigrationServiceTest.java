package com.cis.api.migration;

import com.cis.api.model.User;
import com.cis.api.repository.MongoPersistencePort;
import com.cis.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UserDataMigrationServiceTest {

    @Autowired
    private UserDataMigrationService migrationService;

    @Autowired
    private UserRepository mysqlUserRepository;

    @Autowired
    private MongoPersistencePort mongoPersistencePort;

    @Test
    void testDryRunMigration() {
        // Given
        createTestUserInMysql();

        // When
        var result = migrationService.migrateUsers(true, false);

        // Then
        assertThat(result.totalFound).isGreaterThan(0);
        assertThat(result.successCount).isZero(); // Dry run doesn't save
        assertThat(result.errors).isEmpty();
    }

    @Test
    void testFullMigration() {
        // Given
        createTestUserInMysql();

        // When
        var result = migrationService.migrateUsers(false, true);

        // Then
        assertThat(result.successCount).isEqualTo(result.totalFound);
        assertThat(result.failCount).isZero();
        assertThat(mongoPersistencePort.findAll()).hasSize(result.finalCount);
    }

    private void createTestUserInMysql() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Test User");
        user.setLogin("testuser" + System.currentTimeMillis());
        user.setPassword("password123");
        mysqlUserRepository.save(user);
    }
}