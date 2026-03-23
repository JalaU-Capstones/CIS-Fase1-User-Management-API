package com.cis.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "api.security")
public class AccessLevelProperties {

    /**
     * If true, GET /api/v1/users/** endpoints are publicly accessible.
     * If false, they require JWT authentication.
     * Default: true
     */
    private boolean publicReadEndpoints = true;
}