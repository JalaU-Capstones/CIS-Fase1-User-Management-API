package com.cis.api.dto;

import java.util.UUID;

/**
 * Data Transfer Object for User responses.
 * Excludes sensitive information like passwords.
 *
 * @param id    The unique identifier of the user.
 * @param name  The full name of the user.
 * @param login The login username of the user.
 */
public record UserResponseDto(
    UUID id,
    String name,
    String login
) {}
