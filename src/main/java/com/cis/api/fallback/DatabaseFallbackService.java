package com.cis.api.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Determines the active database (MySQL/MongoDB) to use for a given request.
 * <p>
 * Behavior:
 * <ul>
 *     <li>If both databases are healthy, uses the default mapping (v1 -> MySQL, v2 -> MongoDB).</li>
 *     <li>If one database is unhealthy, routes both API versions to the healthy database and marks fallback active.</li>
 *     <li>If both databases are unhealthy, throws {@link BothDatabasesDownException}.</li>
 * </ul>
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.fallback", name = "enabled", havingValue = "true")
public class DatabaseFallbackService {

    public static final String DB_MYSQL = "mysql";
    public static final String DB_MONGO = "mongo";

    private final DatabaseFallbackProperties properties;
    private final MySqlHealthIndicator mySqlHealthIndicator;
    private final MongoHealthIndicator mongoHealthIndicator;

    private final AtomicReference<String> lastMode = new AtomicReference<>("unknown");

    public DatabaseFallbackService(
            DatabaseFallbackProperties properties,
            MySqlHealthIndicator mySqlHealthIndicator,
            MongoHealthIndicator mongoHealthIndicator
    ) {
        this.properties = properties;
        this.mySqlHealthIndicator = mySqlHealthIndicator;
        this.mongoHealthIndicator = mongoHealthIndicator;
    }

    /**
     * Returns whether the API is currently operating in fallback mode (i.e. one database is down and the other is used
     * for all versions).
     */
    public boolean isFallbackActive() {
        if (!properties.isEnabled()) {
            return false;
        }
        boolean mysqlUp = mySqlHealthIndicator.isUp();
        boolean mongoUp = mongoHealthIndicator.isUp();
        // fallback is active when exactly one is up
        return (mysqlUp ^ mongoUp);
    }

    /**
     * Resolves the active database for the given original version ("v1" or "v2").
     *
     * @param originalVersion expected values: "v1" or "v2" (case-insensitive)
     * @return {@link #DB_MYSQL} or {@link #DB_MONGO}
     * @throws BothDatabasesDownException when both databases are unhealthy and the feature is enabled
     */
    public String getActiveDatabase(String originalVersion) {
        String version = normalizeVersion(originalVersion);
        boolean enabled = properties.isEnabled();

        if (!enabled) {
            return defaultForVersion(version);
        }

        boolean mysqlUp = mySqlHealthIndicator.isUp();
        boolean mongoUp = mongoHealthIndicator.isUp();

        if (mysqlUp && mongoUp) {
            updateMode("normal", "Both databases are healthy");
            return defaultForVersion(version);
        }

        if (mysqlUp) {
            updateMode("fallback:mysql", "MongoDB unhealthy; routing all traffic to MySQL");
            return DB_MYSQL;
        }

        if (mongoUp) {
            updateMode("fallback:mongo", "MySQL unhealthy; routing all traffic to MongoDB");
            return DB_MONGO;
        }

        updateMode("outage", "Both MySQL and MongoDB are unhealthy");
        throw new BothDatabasesDownException("Both databases are down");
    }

    /**
     * Throws {@link BothDatabasesDownException} when both databases are unhealthy and fallback is enabled.
     */
    public void assertAtLeastOneDatabaseUp(String originalVersion) {
        // Force evaluation and exception if both are down.
        getActiveDatabase(originalVersion);
    }

    private String defaultForVersion(String version) {
        return "v2".equals(version) ? DB_MONGO : DB_MYSQL;
    }

    private String normalizeVersion(String originalVersion) {
        if (originalVersion == null || originalVersion.isBlank()) {
            return "v1";
        }
        String v = originalVersion.trim().toLowerCase(Locale.ROOT);
        if (v.startsWith("/api/v2")) {
            return "v2";
        }
        if (v.startsWith("/api/v1")) {
            return "v1";
        }
        if ("v1".equals(v) || "v2".equals(v)) {
            return v;
        }
        return "v1";
    }

    private void updateMode(String mode, String reason) {
        String prev = lastMode.getAndSet(mode);
        if (!mode.equals(prev)) {
            log.warn("Database fallback mode change at {}: {} (prev={}, reason={})",
                    Instant.now(), mode, prev, reason);
        }
    }
}
