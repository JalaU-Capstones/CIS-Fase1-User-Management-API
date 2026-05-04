package com.cis.api.exception;

import com.cis.api.fallback.BothDatabasesDownException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFoundException_ShouldReturn404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(ex);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).isEqualTo("Not found");
    }

    @Test
    void handleAccessDeniedException_ShouldReturn403() {
        AccessDeniedException ex = new AccessDeniedException("Forbidden");
        ResponseEntity<ErrorResponse> response = handler.handleAccessDeniedException(ex);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getError()).isEqualTo("Forbidden");
    }

    @Test
    void handleIllegalArgumentException_ShouldReturn400() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(ex);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
    }

    @Test
    void handleAuthenticationException_ShouldReturn401() {
        Exception ex = new Exception("Auth failed");
        ResponseEntity<ErrorResponse> response = handler.handleAuthenticationException(ex);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
    }

    @Test
    void handleGenericException_ShouldReturn500() {
        Exception ex = new Exception("Unexpected");
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
    }

    @Test
    void handleBothDatabasesDown_ShouldReturn503() {
        BothDatabasesDownException ex = new BothDatabasesDownException("Down");
        ResponseEntity<String> response = handler.handleBothDatabasesDown(ex);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).contains("maintenance team");
    }

    @Test
    void handleValidationExceptions_ShouldReturn400WithDetails() {
        MethodParameter parameter = mock(MethodParameter.class);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
        bindingResult.addError(new FieldError("obj", "field", "error message"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);
        
        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(ex);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getError()).isEqualTo("Validation Failed");
        assertThat(response.getBody().getDetails()).containsKey("field");
        assertThat(response.getBody().getDetails().get("field")).isEqualTo("error message");
    }
}
