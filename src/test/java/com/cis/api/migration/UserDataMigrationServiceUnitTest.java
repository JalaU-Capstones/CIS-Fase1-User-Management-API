package com.cis.api.migration;

import com.cis.api.model.User;
import com.cis.api.repository.MongoUserSpringRepository;
import com.cis.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDataMigrationServiceUnitTest {

    @Mock
    private UserRepository mysqlUserRepository;

    @Mock
    private MongoUserSpringRepository mongoUserRepository;

    @InjectMocks
    private UserDataMigrationService migrationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setName("Test User");
        testUser.setLogin("testuser");
        testUser.setPassword("password123");
    }

    @Test
    void testMigrateUsers_WhenMySQLHasNoUsers_ShouldReturnZero() {
        // Given
        when(mysqlUserRepository.findAll()).thenReturn(new ArrayList<>());

        // When
        var result = migrationService.migrateUsers(false, false);

        // Then
        assertThat(result.totalFound).isZero();
        assertThat(result.successCount).isZero();
        assertThat(result.failCount).isZero();
        verify(mongoUserRepository, never()).save(any());
    }

    @Test
    void testMigrateUsers_DryRun_ShouldNotSaveToMongoDB() {
        // Given
        List<User> users = List.of(testUser);
        when(mysqlUserRepository.findAll()).thenReturn(users);

        // When
        var result = migrationService.migrateUsers(true, false);

        // Then
        assertThat(result.totalFound).isEqualTo(1);
        assertThat(result.successCount).isZero();
        verify(mongoUserRepository, never()).save(any());
    }

    @Test
    void testMigrateUsers_DryRunWithCleanFlag_ShouldNotDeleteMongoData() {
        // Given
        when(mysqlUserRepository.findAll()).thenReturn(List.of(testUser));

        // When
        var result = migrationService.migrateUsers(true, true);

        // Then
        assertThat(result.totalFound).isEqualTo(1);
        assertThat(result.cleanedCount).isZero();
        verify(mongoUserRepository, never()).deleteAll();
        verify(mongoUserRepository, never()).count();
    }

    @Test
    void testMigrateUsers_WhenUserDoesNotExist_ShouldSave() {
        // Given
        List<User> users = List.of(testUser);
        when(mysqlUserRepository.findAll()).thenReturn(users);
        when(mongoUserRepository.existsById(testUser.getId().toString())).thenReturn(false);
        when(mongoUserRepository.existsByLogin(testUser.getLogin())).thenReturn(false);

        // When
        var result = migrationService.migrateUsers(false, false);

        // Then
        assertThat(result.successCount).isEqualTo(1);
        assertThat(result.failCount).isZero();
        verify(mongoUserRepository, times(1)).save(any());
    }

    @Test
    void testMigrateUsers_WhenUserAlreadyExists_ShouldSkip() {
        // Given
        List<User> users = List.of(testUser);
        when(mysqlUserRepository.findAll()).thenReturn(users);
        when(mongoUserRepository.existsById(testUser.getId().toString())).thenReturn(true);

        // When
        var result = migrationService.migrateUsers(false, false);

        // Then
        assertThat(result.skippedCount).isEqualTo(1);
        assertThat(result.successCount).isZero();
        verify(mongoUserRepository, never()).save(any());
    }

    @Test
    void testMigrateUsers_WhenDuplicateLoginExists_ShouldSkip() {
        // Given
        List<User> users = List.of(testUser);
        when(mysqlUserRepository.findAll()).thenReturn(users);
        when(mongoUserRepository.existsById(testUser.getId().toString())).thenReturn(false);
        when(mongoUserRepository.existsByLogin(testUser.getLogin())).thenReturn(true);

        // When
        var result = migrationService.migrateUsers(false, false);

        // Then
        assertThat(result.skippedCount).isEqualTo(1);
        assertThat(result.successCount).isZero();
        verify(mongoUserRepository, never()).save(any());
    }

    @Test
    void testMigrateUsers_WhenMySQLThrowsException_ShouldHandleGracefully() {
        // Given
        when(mysqlUserRepository.findAll()).thenThrow(new DataAccessException("Connection failed") {});

        // When
        var result = migrationService.migrateUsers(false, false);

        // Then
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.errors).isNotEmpty();
    }

    @Test
    void testMigrateUsers_WhenMySQLReturnsNull_ShouldHandleGracefully() {
        // Given
        when(mysqlUserRepository.findAll()).thenReturn(null);

        // When
        var result = migrationService.migrateUsers(false, false);

        // Then
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.errors).hasSize(1);
        assertThat(result.errors.get(0)).contains("mysqlUsers");
    }

    @Test
    void testMigrateUsers_WhenSaveFails_ShouldIncrementFailCount() {
        // Given
        List<User> users = List.of(testUser);
        when(mysqlUserRepository.findAll()).thenReturn(users);
        when(mongoUserRepository.existsById(testUser.getId().toString())).thenReturn(false);
        when(mongoUserRepository.existsByLogin(testUser.getLogin())).thenReturn(false);
        when(mongoUserRepository.save(any())).thenThrow(new RuntimeException("MongoDB error"));

        // When
        var result = migrationService.migrateUsers(false, false);

        // Then
        assertThat(result.failCount).isEqualTo(1);
        assertThat(result.successCount).isZero();
        assertThat(result.errors).isNotEmpty();
    }

    @Test
    void testMigrateUsers_WithCleanFlag_ShouldDeleteExistingAndMigrate() {
        // Given
        List<User> existingUsers = List.of(testUser);
        List<User> mysqlUsers = List.of(testUser);

        when(mysqlUserRepository.findAll()).thenReturn(existingUsers, mysqlUsers);
        when(mongoUserRepository.existsById(testUser.getId().toString())).thenReturn(false);
        when(mongoUserRepository.existsByLogin(testUser.getLogin())).thenReturn(false);
        when(mongoUserRepository.count()).thenReturn(0L, 1L);

        // When
        var result = migrationService.migrateUsers(false, true);

        // Then
        verify(mongoUserRepository, atLeastOnce()).deleteAll();
        verify(mongoUserRepository, times(1)).save(any());
        assertThat(result.totalFound).isEqualTo(1);
    }

    @Test
    void testMigrateUsers_WithCleanFlagAndEmptyMongo_ShouldTrackZeroCleanedUsers() {
        // Given
        when(mysqlUserRepository.findAll()).thenReturn(List.of(testUser));
        when(mongoUserRepository.existsById(testUser.getId().toString())).thenReturn(false);
        when(mongoUserRepository.existsByLogin(testUser.getLogin())).thenReturn(false);
        when(mongoUserRepository.count()).thenReturn(0L, 1L);

        // When
        var result = migrationService.migrateUsers(false, true);

        // Then
        assertThat(result.cleanedCount).isZero();
        assertThat(result.successCount).isEqualTo(1);
        verify(mongoUserRepository).deleteAll();
    }

    @Test
    void testMigrateUsers_WhenDeleteAllFails_ShouldHandleGracefully() {
        // Given
        when(mongoUserRepository.count()).thenReturn(2L);
        doThrow(new RuntimeException("Delete failed")).when(mongoUserRepository).deleteAll();

        // When
        var result = migrationService.migrateUsers(false, true);

        // Then
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.errors).containsExactly("Delete failed");
        verify(mysqlUserRepository, never()).findAll();
    }


    @Test
    void testMigrateUsers_MultipleUsers_ShouldProcessAll() {
        // Given
        User user1 = testUser;
        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setName("User 2");
        user2.setLogin("user2");
        user2.setPassword("pass");

        List<User> users = List.of(user1, user2);
        when(mysqlUserRepository.findAll()).thenReturn(users);
        when(mongoUserRepository.existsById(anyString())).thenReturn(false);
        when(mongoUserRepository.existsByLogin(anyString())).thenReturn(false);

        // When
        var result = migrationService.migrateUsers(false, false);

        // Then
        assertThat(result.successCount).isEqualTo(2);
        assertThat(result.totalFound).isEqualTo(2);
        verify(mongoUserRepository, times(2)).save(any());
    }

    @Test
    void testMigrationResult_GetSummaryAndHasErrors_ShouldReflectState() {
        var result = new UserDataMigrationService.MigrationResult();
        result.totalFound = 4;
        result.successCount = 2;
        result.failCount = 1;
        result.skippedCount = 1;
        result.cleanedCount = 3;
        result.finalCount = 2;

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.getSummary()).isEqualTo(
                "Migration Summary: Total=4, Success=2, Failed=1, Skipped=1, Cleaned=3, Final=2"
        );

        result.errors.add("boom");

        assertThat(result.hasErrors()).isTrue();
    }
}
