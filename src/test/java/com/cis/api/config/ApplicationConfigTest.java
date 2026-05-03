package com.cis.api.config;

import com.cis.api.repository.MongoPersistencePort;
import com.cis.api.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationConfigTest {

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private MongoPersistencePort mongoPersistencePort;

    private ApplicationConfig applicationConfig;

    @BeforeEach
    void setUp() {
        applicationConfig = new ApplicationConfig(customUserDetailsService, mongoPersistencePort);
    }

    @Test
    void authenticationProvider_ShouldBeDaoAuthenticationProvider() {
        AuthenticationProvider provider = applicationConfig.authenticationProvider();
        assertThat(provider).isInstanceOf(DaoAuthenticationProvider.class);
    }

    @Test
    void authenticationManager_ShouldReturnFromConfig() throws Exception {
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager manager = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(manager);

        assertThat(applicationConfig.authenticationManager(authConfig)).isEqualTo(manager);
    }

    @Test
    void passwordEncoder_ShouldEncodeWithBcrypt() {
        PasswordEncoder encoder = applicationConfig.passwordEncoder();
        String rawPassword = "password123";
        String encoded = encoder.encode(rawPassword);

        assertThat(encoded).startsWith("$2a$");
        assertThat(encoder.matches(rawPassword, encoded)).isTrue();
    }

    @Test
    void passwordEncoder_ShouldMatchPlaintext() {
        PasswordEncoder encoder = applicationConfig.passwordEncoder();
        String password = "plaintextPassword";
        
        assertThat(encoder.matches(password, password)).isTrue();
        assertThat(encoder.matches("wrong", password)).isFalse();
    }

    @Test
    void passwordEncoder_ShouldHandleNullOrEmpty() {
        PasswordEncoder encoder = applicationConfig.passwordEncoder();
        
        assertThat(encoder.matches("any", null)).isFalse();
        assertThat(encoder.matches("any", "")).isFalse();
    }

    @Test
    void passwordEncoder_ShouldMatchVariousBcryptPrefixes() {
        PasswordEncoder encoder = applicationConfig.passwordEncoder();
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        String password = "password";
        
        String hash2a = bcrypt.encode(password);
        String hash2b = hash2a.replaceFirst("\\$2a\\$", "\\$2b\\$");
        String hash2y = hash2a.replaceFirst("\\$2a\\$", "\\$2y\\$");

        assertThat(encoder.matches(password, hash2a)).isTrue();
        assertThat(encoder.matches(password, hash2b)).isTrue();
        assertThat(encoder.matches(password, hash2y)).isTrue();
    }

    @Test
    void userDetailsService_ShouldFindInMySqlFirst() {
        String login = "mysqlUser";
        org.springframework.security.core.userdetails.User mysqlUser = 
                new org.springframework.security.core.userdetails.User(login, "p", Collections.emptyList());
        when(customUserDetailsService.loadUserByUsername(login)).thenReturn(mysqlUser);

        UserDetailsService service = applicationConfig.userDetailsService();
        assertThat(service.loadUserByUsername(login)).isEqualTo(mysqlUser);
    }

    @Test
    void userDetailsService_ShouldFallbackToMongoIfNotFoundInMySql() {
        String login = "mongoUser";
        when(customUserDetailsService.loadUserByUsername(login)).thenThrow(new UsernameNotFoundException("Not found"));
        
        com.cis.api.model.User mongoUser = new com.cis.api.model.User(java.util.UUID.randomUUID(), "Mongo", login, "p");
        when(mongoPersistencePort.findByLogin(login)).thenReturn(java.util.Optional.of(mongoUser));

        UserDetailsService service = applicationConfig.userDetailsService();
        org.springframework.security.core.userdetails.UserDetails result = service.loadUserByUsername(login);
        
        assertThat(result.getUsername()).isEqualTo(login);
        assertThat(result.getPassword()).isEqualTo("p");
    }

    @Test
    void userDetailsService_ShouldThrowIfNotFoundInBoth() {
        String login = "none";
        when(customUserDetailsService.loadUserByUsername(login)).thenThrow(new UsernameNotFoundException("Not found"));
        when(mongoPersistencePort.findByLogin(login)).thenReturn(java.util.Optional.empty());

        UserDetailsService service = applicationConfig.userDetailsService();
        org.junit.jupiter.api.Assertions.assertThrows(UsernameNotFoundException.class, 
                () -> service.loadUserByUsername(login));
    }
}
