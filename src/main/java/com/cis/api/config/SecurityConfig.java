package com.cis.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the User Management API.
 * Configures endpoint access rules and disables CSRF for REST stateless communication.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain.
     * - Disables CSRF (stateless API).
     * - Allows public access to GET /api/v1/users (R6).
     * - Allows public access to GET /api/v1/users/{id} (R6).
     * - Requires authentication for all other endpoints (future scope).
     *
     * @param http the HttpSecurity object to configure.
     * @return the configured SecurityFilterChain.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/users").permitAll()
                .requestMatchers("/api/v1/users/**").permitAll()
                .anyRequest().authenticated()
            )
            .build();
    }
}
