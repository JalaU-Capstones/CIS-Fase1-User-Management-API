package com.cis.api.service;

import com.cis.api.dto.AuthRequest;
import com.cis.api.dto.AuthResponse;
import com.cis.api.repository.MongoPersistencePort;
import com.cis.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class MongoAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final MongoPersistencePort mongoPersistencePort;
    private final JwtService jwtService;

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.login(),
                        request.password()
                )
        );

        var user = mongoPersistencePort.findByLogin(request.login())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + request.login()));

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getLogin(),
                user.getPassword(),
                Collections.emptyList()
        );

        String jwtToken = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }
}
