package com.cis.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Data Transfer Object for User creation and update requests.
 * Consolidates input validation rules.
 */
@Schema(description = "User request data")
@Builder
public record UserRequestDto(
        @Schema(description = "Full name of the user", example = "John Doe")
        @NotBlank(message = "Name is required")
        @Size(max = 200, message = "Name must be at most 200 characters")
        String name,

        @Schema(description = "Username used for login", example = "johndoe")
        @NotBlank(message = "Login is required")
        @Size(max = 20, message = "Login must be at most 20 characters")
        String login,

        @Schema(description = "Password used for login", example = "Password123")
        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
        String password
) {}