package com.cis.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the User Management API.
 * Intercepts exceptions thrown by any controller and returns
 * a consistent JSON error response instead of Spring's default error page.
 *
 * @RestControllerAdvice tells Spring to apply this handler to all controllers.
 * @ExceptionHandler tells Spring which exception this method handles.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Handles ResourceNotFoundException and returns HTTP 404
     * with a JSON body containing the status and error message.
     *
     * @param ex the exception thrown when a resource is not found
     * @return ResponseEntity with status 404 and error details
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 404);
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
