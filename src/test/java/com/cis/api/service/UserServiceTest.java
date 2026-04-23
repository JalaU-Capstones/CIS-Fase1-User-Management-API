package com.cis.api.service;

import com.cis.api.dto.UserRequestDto;
import com.cis.api.dto.UserResponseDto;
import com.cis.api.exception.ResourceNotFoundException;
import com.cis.api.model.User;
import com.cis.api.repository.UserPersistencePort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthentication(Object principal) {
        given(securityContext.getAuthentication()).willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(principal);
    }

    @Test
    void shouldReturnListOfUsersAsDtos() {
        User user = new User(UUID.randomUUID(), "Test User", "test", "pass");
        given(userPersistencePort.findAll()).willReturn(List.of(user));

        List<UserResponseDto> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).login()).isEqualTo("test");
        assertThat(result.get(0).id()).isEqualTo(user.getId());
        then(userPersistencePort).should().findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoUsers() {
        given(userPersistencePort.findAll()).willReturn(Collections.emptyList());

        List<UserResponseDto> result = userService.getAllUsers();

        assertThat(result).isEmpty();
    }
    
    @Test
    void shouldGetUserById() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "Test", "test", "pass");
        given(userPersistencePort.findById(id)).willReturn(Optional.of(user));
        
        UserResponseDto result = userService.getUserById(id.toString());
        
        assertThat(result.id()).isEqualTo(id);
    }
    
    @Test
    void shouldCreateUser() {
        UserRequestDto request = new UserRequestDto("Name", "login", "pass");
        User savedUser = new User(UUID.randomUUID(), "Name", "login", "encodedPass");
        
        given(userPersistencePort.existsByLogin("login")).willReturn(false);
        given(passwordEncoder.encode("pass")).willReturn("encodedPass");
        given(userPersistencePort.save(any(User.class))).willReturn(savedUser);
        
        UserResponseDto response = userService.createUser(request);
        
        assertThat(response.login()).isEqualTo("login");
        assertThat(response.name()).isEqualTo("Name");
    }

    @Test
    void shouldThrowWhenCreatingDuplicateLogin() {
        UserRequestDto request = new UserRequestDto("Name", "login", "pass");
        given(userPersistencePort.existsByLogin("login")).willReturn(true);
        
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Login already exists");
    }
    
    @Test
    void shouldUpdateUserWhenOwner() {
        UUID id = UUID.randomUUID();
        String login = "owner";
        UserRequestDto request = new UserRequestDto("New Name", login, "newpass");
        User existingUser = new User(id, "Old Name", login, "pass");
        User updatedUser = new User(id, "New Name", login, "encodedNewPass");

        mockAuthentication(login);
        given(userPersistencePort.findById(id)).willReturn(Optional.of(existingUser));
        given(passwordEncoder.encode("newpass")).willReturn("encodedNewPass");
        given(userPersistencePort.save(existingUser)).willReturn(updatedUser);

        UserResponseDto response = userService.updateUser(id.toString(), request);
        
        assertThat(response.name()).isEqualTo("New Name");
    }

    @Test
    void shouldUpdateUserWithoutChangingPasswordWhenBlank() {
        UUID id = UUID.randomUUID();
        String login = "owner";
        UserRequestDto request = new UserRequestDto("New Name", login, "");
        User existingUser = new User(id, "Old Name", login, "pass");
        User updatedUser = new User(id, "New Name", login, "pass");

        mockAuthentication(login);
        given(userPersistencePort.findById(id)).willReturn(Optional.of(existingUser));
        given(userPersistencePort.save(existingUser)).willReturn(updatedUser);

        UserResponseDto response = userService.updateUser(id.toString(), request);
        
        assertThat(response.name()).isEqualTo("New Name");
        then(passwordEncoder).should(never()).encode(any());
    }

    @Test
    void shouldThrowWhenUpdatingWithDuplicateLogin() {
        UUID id = UUID.randomUUID();
        String login = "owner";
        UserRequestDto request = new UserRequestDto("New Name", "newlogin", null);
        User existingUser = new User(id, "Old Name", login, "pass");

        mockAuthentication(login);
        given(userPersistencePort.findById(id)).willReturn(Optional.of(existingUser));
        given(userPersistencePort.existsByLoginAndIdNot("newlogin", id)).willReturn(true);

        assertThatThrownBy(() -> userService.updateUser(id.toString(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Login already exists");
    }

    @Test
    void shouldThrowWhenUpdatingAnotherUser() {
        UUID id = UUID.randomUUID();
        UserRequestDto request = new UserRequestDto("New Name", "other", null);
        User existingUser = new User(id, "Old Name", "other", "pass");

        mockAuthentication("not-owner");
        given(userPersistencePort.findById(id)).willReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.updateUser(id.toString(), request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You can only modify your own user record");
    }

    @Test
    void shouldDeleteUserWhenOwner() {
        UUID id = UUID.randomUUID();
        String idStr = id.toString();
        String login = "owner";
        User user = new User(id, "Test", login, "pass");
        
        mockAuthentication(login);
        given(userPersistencePort.findById(id)).willReturn(Optional.of(user));
        
        userService.deleteUser(idStr);
        
        then(userPersistencePort).should().deleteUserAndRelatedData(id);
    }

    @Test
    void shouldThrowWhenDeletingAnotherUser() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "Test", "other", "pass");

        mockAuthentication("not-owner");
        given(userPersistencePort.findById(id)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.deleteUser(id.toString()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You can only modify your own user record");
    }

    @Test
    void shouldThrowWhenDeletingNonExistentUser() {
        UUID id = UUID.randomUUID();
        given(userPersistencePort.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(id.toString()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
        
        then(userPersistencePort).should(never()).deleteUserAndRelatedData(any());
    }
}
