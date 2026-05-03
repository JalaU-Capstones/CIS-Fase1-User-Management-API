package com.cis.api.config;

import com.cis.api.console.ConsoleCommandListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("MigrationStateInitializer")
class MigrationStateInitializerTest {

    private ConsoleCommandListener mockConsoleListener;

    @BeforeEach
    void setUp() {
        mockConsoleListener = mock(ConsoleCommandListener.class);
    }

    @AfterEach
    void clearProperties() {
        System.clearProperty("migration.maintenance");
        System.clearProperty("sunset.v1");
    }

    @Test
    @DisplayName("sets both flags from system properties and starts console listener")
    void setsFlagsFromSystemProperties() throws Exception {
        System.setProperty("migration.maintenance", "true");
        System.setProperty("sunset.v1", "true");

        SystemStateConfig config = new SystemStateConfig();
        new MigrationStateInitializer(config, mockConsoleListener).run();

        assertTrue(config.isMigrationRunning());
        assertTrue(config.isV1Sunset());
        verify(mockConsoleListener).start();
    }

    @Test
    @DisplayName("absent properties leave flags as default false")
    void absentProperties_doNotChangeDefaults() throws Exception {
        SystemStateConfig config = new SystemStateConfig();
        new MigrationStateInitializer(config, mockConsoleListener).run();

        assertFalse(config.isMigrationRunning());
        assertFalse(config.isV1Sunset());
        verify(mockConsoleListener).start();
    }

    @Test
    @DisplayName("mixed properties set only provided flag")
    void mixedProperties_setOnlyProvided() throws Exception {
        System.setProperty("migration.maintenance", "true");
        SystemStateConfig config = new SystemStateConfig();
        new MigrationStateInitializer(config, mockConsoleListener).run();

        assertTrue(config.isMigrationRunning());
        assertFalse(config.isV1Sunset());
        verify(mockConsoleListener).start();
    }
}
