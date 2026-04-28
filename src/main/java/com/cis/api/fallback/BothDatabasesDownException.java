package com.cis.api.fallback;

/**
 * Exception thrown when both MySQL and MongoDB are unavailable and the API cannot serve requests.
 */
public class BothDatabasesDownException extends RuntimeException {

    public BothDatabasesDownException(String message) {
        super(message);
    }
}

