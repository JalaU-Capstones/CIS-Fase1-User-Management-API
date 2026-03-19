package com.cis.api.dto;

import java.util.UUID;

/**
 * Data Transfer Object for User responses.
 * Excludes sensitive information like passwords.
 */
public record UserResponseDto(
    UUID id,
    String name,
    String login
) {}
