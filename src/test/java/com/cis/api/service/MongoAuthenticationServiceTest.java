package com.cis.api.service;

import com.cis.api.dto.AuthRequest;
import com.cis.api.dto.AuthResponse;
import com.cis.api.model.User;
import com.cis.api.repository.MongoPersistencePort;
import com.cis.api.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class MongoAuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private MongoPersistencePort mongoPersistencePort;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private MongoAuthenticationService authenticationService;

    @Test
    void shouldAuthenticateAndReturnToken() {
        AuthRequest request = new AuthRequest("mongo-user", "pass");
        User user = new User(UUID.randomUUID(), "Mongo User", "mongo-user", "hashed-pass");

        given(mongoPersistencePort.findByLogin("mongo-user")).willReturn(Optional.of(user));
        given(jwtService.generateToken(any(UserDetails.class))).willReturn("jwt-token-mongo");

        AuthResponse response = authenticationService.authenticate(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token-mongo");
        then(authenticationManager).should().authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        AuthRequest request = new AuthRequest("nonexistent", "pass");
        given(mongoPersistencePort.findByLogin("nonexistent")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.authenticate(request))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
