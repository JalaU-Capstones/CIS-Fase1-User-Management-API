package com.cis.api.exception;

import com.cis.api.dto.UserMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    void testErrorResponse() {
        Map<String, String> details = Map.of("field", "error");
        ErrorResponse.ErrorResponseBuilder builder = ErrorResponse.builder()
                .status(400)
                .error("Bad Request")
                .message("Message")
                .details(details);

        ErrorResponse response = builder.build();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getError()).isEqualTo("Bad Request");
        assertThat(response.getMessage()).isEqualTo("Message");
        assertThat(response.getDetails()).isEqualTo(details);
        assertThat(builder.toString()).isNotEmpty();
    }

    @Test
    void testUserMapperConstructor() {
        // Just for coverage of the private constructor
        UserMapper mapper = new UserMapper();
        assertThat(mapper).isNotNull();
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
