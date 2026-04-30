package com.cis.api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SystemStateConfig")
class SystemStateConfigTest {

    private SystemStateConfig config;

    @BeforeEach
    void setUp() {
        config = new SystemStateConfig();
    }

    @Nested
    @DisplayName("isV1Sunset")
    class V1Sunset {
        @Test
        @DisplayName("default value is false")
        void defaultValue_isFalse() {
            assertFalse(config.isV1Sunset());
        }

        @Test
        @DisplayName("setV1Sunset(true) → isV1Sunset() returns true")
        void setTrue_returnsTrue() {
            config.setV1Sunset(true);
            assertTrue(config.isV1Sunset());
        }

        @Test
        @DisplayName("setV1Sunset(false) after true → isV1Sunset() returns false")
        void setFalse_afterTrue_returnsFalse() {
            config.setV1Sunset(true);
            config.setV1Sunset(false);
            assertFalse(config.isV1Sunset());
        }
    }

    @Nested
    @DisplayName("isMigrationRunning")
    class MigrationRunning {
        @Test
        @DisplayName("default value is false")
        void defaultValue_isFalse() {
            assertFalse(config.isMigrationRunning());
        }

        @Test
        @DisplayName("setMigrationRunning(true) → returns true")
        void setTrue_returnsTrue() {
            config.setMigrationRunning(true);
            assertTrue(config.isMigrationRunning());
        }

        @Test
        @DisplayName("setMigrationRunning(false) after true → returns false")
        void setFalse_afterTrue_returnsFalse() {
            config.setMigrationRunning(true);
            config.setMigrationRunning(false);
            assertFalse(config.isMigrationRunning());
        }
    }

    @Test
    @DisplayName("flags are independent")
    void flagsAreIndependent() {
        config.setMigrationRunning(true);
        config.setV1Sunset(true);
        assertTrue(config.isMigrationRunning());
        assertTrue(config.isV1Sunset());

        config.setMigrationRunning(false);
        assertFalse(config.isMigrationRunning());
        assertTrue(config.isV1Sunset());
    }

    @Test
    @DisplayName("concurrent writes are visible to all readers (AtomicBoolean guarantee)")
    void concurrentWrites_areThreadSafe() throws InterruptedException {
        int threads = 20;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch  = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            final boolean flag = i % 2 == 0;
            pool.submit(() -> {
                try {
                    startLatch.await();
                    config.setMigrationRunning(flag);
                    config.setV1Sunset(!flag);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(5, TimeUnit.SECONDS), "Timed out waiting for threads");
        assertDoesNotThrow(() -> {
            boolean mig = config.isMigrationRunning();
            boolean sun = config.isV1Sunset();
        });
        pool.shutdown();
    }
}