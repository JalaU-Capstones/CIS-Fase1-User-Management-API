package com.cis.api.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Health indicator for MySQL connectivity.
 * <p>
 * Uses a cached health value refreshed at most once per TTL to avoid excessive database calls.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.fallback", name = "enabled", havingValue = "true")
public class MySqlHealthIndicator implements HealthIndicator {

    private static final ExecutorService HEALTH_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private final JdbcTemplate jdbcTemplate;
    private final DatabaseFallbackProperties properties;

    private final AtomicReference<Health> cached = new AtomicReference<>(Health.unknown().build());
    private final AtomicLong lastRefreshMs = new AtomicLong(0L);

    public MySqlHealthIndicator(JdbcTemplate jdbcTemplate, DatabaseFallbackProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
    }

    @Override
    public Health health() {
        long now = System.currentTimeMillis();
        long last = lastRefreshMs.get();
        if (now - last < properties.getHealthTtlMs()) {
            return cached.get();
        }
        return refresh(now);
    }

    /**
     * Returns whether MySQL is currently healthy (UP) according to the cached check.
     */
    public boolean isUp() {
        return Health.up().build().getStatus().equals(health().getStatus());
    }

    private Health refresh(long now) {
        // ensure only one thread does the refresh work per TTL window
        long last = lastRefreshMs.get();
        if (now - last < properties.getHealthTtlMs()) {
            return cached.get();
        }

        try {
            Integer one = runWithTimeout(() -> jdbcTemplate.queryForObject("SELECT 1", Integer.class));
            Health health = (one != null && one == 1)
                    ? Health.up().build()
                    : Health.down().withDetail("reason", "Unexpected SELECT 1 result").build();
            cached.set(health);
            lastRefreshMs.set(now);
            return health;
        } catch (TimeoutException ex) {
            log.debug("MySQL health check timed out after {} ms", properties.getHealthTimeoutMs(), ex);
            Health health = Health.down().withDetail("reason", "timeout").build();
            cached.set(health);
            lastRefreshMs.set(now);
            return health;
        } catch (Exception ex) {
            log.debug("MySQL health check failed", ex);
            Health health = Health.down().build();
            cached.set(health);
            lastRefreshMs.set(now);
            return health;
        }
    }

    private <T> T runWithTimeout(Callable<T> callable) throws Exception {
        Future<T> future = HEALTH_EXECUTOR.submit(callable);
        try {
            return future.get(properties.getHealthTimeoutMs(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            future.cancel(true);
            throw ex;
        }
    }
}
