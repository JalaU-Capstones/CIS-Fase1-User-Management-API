package com.cis.api.service;

import com.cis.api.dto.AuthRequest;
import com.cis.api.dto.AuthResponse;
import com.cis.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.login(),
                        request.password()
                )
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.login());
        String jwtToken = jwtService.generateToken(userDetails);

        String message = null;
        String encodedPassword = userDetails.getPassword();
        if (encodedPassword == null ||
                !(encodedPassword.startsWith("$2a$") ||
                  encodedPassword.startsWith("$2b$") ||
                  encodedPassword.startsWith("$2y$"))) {
            message = "Login successful. For security reasons, please change your password to enable hashing.";
        }

        return AuthResponse.builder()
                .token(jwtToken)
                .message(message)
                .build();
    }
}
