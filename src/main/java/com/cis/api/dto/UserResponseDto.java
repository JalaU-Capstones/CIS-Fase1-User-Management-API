package com.cis.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Data Transfer Object for User responses.
 * Excludes sensitive information like passwords.
 */
@Schema(description = "User response data")
public record UserResponseDto(
    @Schema(description = "Unique identifier of the user", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,
    @Schema(description = "Full name of the user", example = "John Doe")
    String name,
    @Schema(description = "Username used for login", example = "johndoe")
    String login
) {}
