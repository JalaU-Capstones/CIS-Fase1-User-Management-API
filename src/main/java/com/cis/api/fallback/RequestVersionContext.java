package com.cis.api.fallback;

/**
 * Stores the original API version for the current request (v1/v2).
 * <p>
 * This is used by routing components to decide which persistence adapter to delegate to.
 */
public final class RequestVersionContext {

    private static final ThreadLocal<String> ORIGINAL_VERSION = new ThreadLocal<>();

    private RequestVersionContext() {
    }

    /**
     * Sets the original version for the current thread/request.
     *
     * @param originalVersion expected values: "v1" or "v2"
     */
    public static void setOriginalVersion(String originalVersion) {
        ORIGINAL_VERSION.set(originalVersion);
    }

    /**
     * Gets the original version for the current thread/request.
     *
     * @return "v1", "v2", or null if not set
     */
    public static String getOriginalVersion() {
        return ORIGINAL_VERSION.get();
    }

    /**
     * Clears the current thread/request context.
     */
    public static void clear() {
        ORIGINAL_VERSION.remove();
    }
}

