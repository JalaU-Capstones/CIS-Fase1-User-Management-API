package com.cis.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Authentication response data")
public record AuthRequest(
    @Schema(description = "Username used for login", example = "johndoe")
    @NotBlank(message = "Login is required")
    String login,
    @Schema(description = "Password used for login", example = "Password123")
    @NotBlank(message = "Password is required")
    String password
) {}
