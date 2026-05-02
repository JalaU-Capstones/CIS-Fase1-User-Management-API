package com.cis.api.fallback;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the emergency database fallback feature.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.fallback")
public class DatabaseFallbackProperties {

    /**
     * Feature toggle. When disabled, the API never switches databases.
     */
    private boolean enabled = true;

    /**
     * Health cache refresh interval in milliseconds.
     */
    private long healthTtlMs = 10_000L;

    /**
     * Maximum time (in milliseconds) allowed for a single health check call before it is treated as DOWN.
     * <p>
     * This prevents requests from blocking for long driver timeouts when a database is unavailable.
     */
    private long healthTimeoutMs = 2_000L;
}
