package com.cis.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    @Test
    void shouldGenerateToken() {
        UserDetails userDetails = new User("testuser", "password", new ArrayList<>());
        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void shouldExtractUsername() {
        UserDetails userDetails = new User("testuser", "password", new ArrayList<>());
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void shouldValidateToken() {
        UserDetails userDetails = new User("testuser", "password", new ArrayList<>());
        String token = jwtService.generateToken(userDetails);
        
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }
}
