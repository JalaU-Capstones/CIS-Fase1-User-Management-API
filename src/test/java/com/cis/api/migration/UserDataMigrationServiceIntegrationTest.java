package com.cis.api.migration;

import com.cis.api.model.MongoUser;
import com.cis.api.model.User;
import com.cis.api.repository.MongoUserSpringRepository;
import com.cis.api.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("Requires Docker - Testcontainers not available in CI/CD pipeline. Run manually with Docker installed.")
@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDataMigrationServiceIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:6.0"))
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.data.mongodb.database", () -> "testdb");
    }

    @Autowired
    private UserDataMigrationService migrationService;

    @Autowired
    private UserRepository mysqlUserRepository;

    @Autowired
    private MongoUserSpringRepository mongoUserRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean repositories before each test
        mongoUserRepository.deleteAll();

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setName("Integration Test User");
        testUser.setLogin("int_test_" + System.currentTimeMillis() % 10000);
        testUser.setPassword("password123");
        testUser = mysqlUserRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        // Clean up
        try {
            mysqlUserRepository.deleteById(testUser.getId());
        } catch (Exception e) {
            // User might have been deleted already
        }
        mongoUserRepository.deleteAll();
    }

    @Test
    @Order(1)
    void testDryRunMigration_DoesNotSaveToMongoDB() {
        // When
        var result = migrationService.migrateUsers(true, false);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.totalFound).isGreaterThan(0);
        assertThat(result.successCount).isZero();
        assertThat(mongoUserRepository.count()).isZero();
    }

    @Test
    @Order(2)
    void testFullMigration_MigratesUserSuccessfully() {
        // When
        var result = migrationService.migrateUsers(false, false);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.successCount).isEqualTo(1);
        assertThat(result.failCount).isZero();
        assertThat(mongoUserRepository.count()).isEqualTo(1);

        // Verify user structure matches expected schema
        MongoUser migratedUser = mongoUserRepository.findAll().get(0);
        assertThat(migratedUser.getId()).isEqualTo(testUser.getId().toString());
        assertThat(migratedUser.getName()).isEqualTo(testUser.getName());
        assertThat(migratedUser.getLogin()).isEqualTo(testUser.getLogin());
        assertThat(migratedUser.getPassword()).isEqualTo(testUser.getPassword());
    }

    @Test
    @Order(3)
    void testMigration_IsIdempotent() {
        // First migration
        var firstResult = migrationService.migrateUsers(false, false);
        assertThat(firstResult.successCount).isEqualTo(1);
        assertThat(mongoUserRepository.count()).isEqualTo(1);

        // Second migration (should skip existing)
        var secondResult = migrationService.migrateUsers(false, false);
        assertThat(secondResult.successCount).isZero();
        assertThat(secondResult.skippedCount).isEqualTo(1);
        assertThat(mongoUserRepository.count()).isEqualTo(1);
    }

    @Test
    @Order(4)
    void testMigration_WithCleanFlag_ReplacesData() {
        // First migration
        var firstResult = migrationService.migrateUsers(false, false);
        assertThat(firstResult.successCount).isEqualTo(1);
        assertThat(mongoUserRepository.count()).isEqualTo(1);

        // Create another user in MySQL
        User secondUser = new User();
        secondUser.setId(UUID.randomUUID());
        secondUser.setName("Second Test User");
        secondUser.setLogin("second_user_" + System.currentTimeMillis() % 10000);
        secondUser.setPassword("password456");
        secondUser = mysqlUserRepository.save(secondUser);

        // Clean and migrate
        var cleanResult = migrationService.migrateUsers(false, true);

        // After clean, should have exactly 1 user (the second one)
        // Because clean removes all, then migrates current MySQL users
        assertThat(cleanResult.totalFound).isEqualTo(2); // Both users in MySQL
        assertThat(cleanResult.cleanedCount).isEqualTo(1);
        assertThat(mongoUserRepository.count()).isEqualTo(2); // Both migrated after clean

        // Clean up second user
        mysqlUserRepository.deleteById(secondUser.getId());
    }

    @Test
    @Order(5)
    void testMigration_HandlesDuplicateLoginGracefully() {
        // First migration
        migrationService.migrateUsers(false, false);

        // Try to migrate same data again
        var result = migrationService.migrateUsers(false, false);

        // Should skip duplicates, not fail
        assertThat(result.failCount).isZero();
        assertThat(result.skippedCount).isGreaterThan(0);
        assertThat(result.successCount).isZero();
    }

    @Test
    @Order(6)
    void testMongoUserSchema_MatchesExpectedFormat() {
        // Migrate user
        migrationService.migrateUsers(false, false);

        // Get migrated user
        MongoUser migratedUser = mongoUserRepository.findAll().get(0);

        // Verify schema matches C# API expectations
        assertThat(migratedUser.getId()).isNotNull();
        assertThat(migratedUser.getId()).isNotBlank();
        assertThat(migratedUser.getId()).doesNotContain("ObjectId");

        // Field names should be exactly as expected
        // Using reflection to verify field names
        var fields = MongoUser.class.getDeclaredFields();
        var fieldNames = java.util.Arrays.stream(fields)
                .map(java.lang.reflect.Field::getName)
                .collect(java.util.stream.Collectors.toSet());

        assertThat(fieldNames).contains("id", "name", "login", "password");

        // Verify MongoDB document has correct field names
        var mongoDoc = mongoUserRepository.findById(migratedUser.getId()).orElseThrow();
        assertThat(mongoDoc.getName()).isNotNull(); // Should be accessible via getter
    }
}