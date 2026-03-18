package com.cis.api.dto;

import lombok.Builder;

@Builder
public record AuthResponse(
    String token
) {}
