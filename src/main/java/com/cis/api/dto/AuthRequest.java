package com.cis.api.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
    @NotBlank(message = "Login is required")
    String login,

    @NotBlank(message = "Password is required")
    String password
) {}
