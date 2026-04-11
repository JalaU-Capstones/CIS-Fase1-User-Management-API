package com.cis.api.service;

import com.cis.api.dto.AuthRequest;
import com.cis.api.dto.AuthResponse;
import com.cis.api.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void shouldAuthenticateAndReturnTokenWithBcrypt() {
        AuthRequest request = new AuthRequest("user", "pass");
        UserDetails userDetails = mock(UserDetails.class);
        
        given(userDetailsService.loadUserByUsername("user")).willReturn(userDetails);
        given(userDetails.getPassword()).willReturn("$2a$10$somehashedpassword");
        given(jwtService.generateToken(userDetails)).willReturn("jwt-token");

        AuthResponse response = authenticationService.authenticate(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.message()).isNull();
        then(authenticationManager).should().authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void shouldAuthenticateAndReturnTokenWithPlaintextWarning() {
        AuthRequest request = new AuthRequest("user", "pass");
        UserDetails userDetails = mock(UserDetails.class);

        given(userDetailsService.loadUserByUsername("user")).willReturn(userDetails);
        given(userDetails.getPassword()).willReturn("plaintext-password");
        given(jwtService.generateToken(userDetails)).willReturn("jwt-token");

        AuthResponse response = authenticationService.authenticate(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.message()).contains("For security reasons, please change your password");
        then(authenticationManager).should().authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
