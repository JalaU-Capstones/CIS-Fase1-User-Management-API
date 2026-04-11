package com.cis.api.exception;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    void testErrorResponseBuilder() {
        Map<String, String> details = Map.of("field", "error");
        ErrorResponse response = ErrorResponse.builder()
                .status(404)
                .error("Not Found")
                .message("User not found")
                .details(details)
                .build();

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getError()).isEqualTo("Not Found");
        assertThat(response.getMessage()).isEqualTo("User not found");
        assertThat(response.getDetails()).isEqualTo(details);
    }

    @Test
    void testErrorResponseNoDetails() {
        ErrorResponse response = ErrorResponse.builder()
                .status(500)
                .error("Internal Server Error")
                .message("Error occurred")
                .build();

        assertThat(response.getDetails()).isNull();
    }
}
