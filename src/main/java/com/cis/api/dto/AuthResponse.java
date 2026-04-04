package com.cis.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Authentication response data")
@Builder
public record AuthResponse(
    @Schema(description = "JWT token generated after successful login", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huZG9lIn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
    String token,

    @Schema(description = "A friendly message for the user", example = "Login successful.")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String message
) {}
