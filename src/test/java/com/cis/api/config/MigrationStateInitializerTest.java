package com.cis.api.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MigrationStateInitializer")
class MigrationStateInitializerTest {

    @AfterEach
    void clearProperties() {
        System.clearProperty("migration.maintenance");
        System.clearProperty("sunset.v1");
    }

    @Test
    @DisplayName("sets both flags from system properties")
    void setsFlagsFromSystemProperties() throws Exception {
        System.setProperty("migration.maintenance", "true");
        System.setProperty("sunset.v1", "true");

        SystemStateConfig config = new SystemStateConfig();
        new MigrationStateInitializer(config).run();

        assertTrue(config.isMigrationRunning());
        assertTrue(config.isV1Sunset());
    }

    @Test
    @DisplayName("absent properties leave flags as default false")
    void absentProperties_doNotChangeDefaults() throws Exception {
        SystemStateConfig config = new SystemStateConfig();
        new MigrationStateInitializer(config).run();

        assertFalse(config.isMigrationRunning());
        assertFalse(config.isV1Sunset());
    }

    @Test
    @DisplayName("mixed properties set only provided flag")
    void mixedProperties_setOnlyProvided() throws Exception {
        System.setProperty("migration.maintenance", "true");
        SystemStateConfig config = new SystemStateConfig();
        new MigrationStateInitializer(config).run();

        assertTrue(config.isMigrationRunning());
        assertFalse(config.isV1Sunset());
    }
}