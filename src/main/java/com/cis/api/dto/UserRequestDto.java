package com.cis.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Data Transfer Object for User creation and update requests.
 * Consolidates input validation rules.
 */
@Builder
public record UserRequestDto(
        @NotBlank(message = "Name is required")
        @Size(max = 200, message = "Name must be at most 200 characters")
        String name,

        @NotBlank(message = "Login is required")
        @Size(max = 20, message = "Login must be at most 20 characters")
        String login,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
        String password
) {}