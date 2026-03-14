package com.cis.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
/**
 * Exception thrown when a requested resource is not found in the database.
 *
 * The @ResponseStatus tells Spring to automatically return
 * HTTP 404 Not Found when this exception is thrown, instead of the
 * default HTTP 500 Internal Server Error.
 *
 * HttpStatus.NOT_FOUND is a Spring constant that represents HTTP 404.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    /**
     * Creates a new ResourceNotFoundException with a descriptive message.
     * Example: "User not found with id: 123"
     *
     * @param message Description of what resource was not found.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
