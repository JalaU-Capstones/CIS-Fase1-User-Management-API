package com.cis.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Authentication request data")
@Builder
public record AuthResponse(
    @Schema(description = "JWT token generated after successful login", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huZG9lIn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
    String token
) {}
