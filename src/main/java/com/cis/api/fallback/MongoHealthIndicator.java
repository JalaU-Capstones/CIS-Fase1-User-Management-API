package com.cis.api.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
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
 * Health indicator for MongoDB connectivity.
 * <p>
 * Uses a cached health value refreshed at most once per TTL to avoid excessive calls.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.fallback", name = "enabled", havingValue = "true")
public class MongoHealthIndicator implements HealthIndicator {

    private static final ExecutorService HEALTH_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private final MongoTemplate mongoTemplate;
    private final DatabaseFallbackProperties properties;

    private final AtomicReference<Health> cached = new AtomicReference<>(Health.unknown().build());
    private final AtomicLong lastRefreshMs = new AtomicLong(0L);

    public MongoHealthIndicator(MongoTemplate mongoTemplate, DatabaseFallbackProperties properties) {
        this.mongoTemplate = mongoTemplate;
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
     * Returns whether MongoDB is currently healthy (UP) according to the cached check.
     */
    public boolean isUp() {
        return Health.up().build().getStatus().equals(health().getStatus());
    }

    private Health refresh(long now) {
        long last = lastRefreshMs.get();
        if (now - last < properties.getHealthTtlMs()) {
            return cached.get();
        }

        try {
            runWithTimeout(() -> {
                mongoTemplate.executeCommand("{ ping: 1 }");
                return null;
            });
            Health health = Health.up().build();
            cached.set(health);
            lastRefreshMs.set(now);
            return health;
        } catch (TimeoutException ex) {
            log.debug("MongoDB health check timed out after {} ms", properties.getHealthTimeoutMs(), ex);
            Health health = Health.down().withDetail("reason", "timeout").build();
            cached.set(health);
            lastRefreshMs.set(now);
            return health;
        } catch (Exception ex) {
            log.debug("MongoDB health check failed", ex);
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
