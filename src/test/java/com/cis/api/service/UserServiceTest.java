package com.cis.api.service;

import com.cis.api.dto.UserCreateRequest;
import com.cis.api.dto.UserResponseDto;
import com.cis.api.dto.UserUpdateRequest;
import com.cis.api.exception.ResourceNotFoundException;
import com.cis.api.model.User;
import com.cis.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnListOfUsersAsDtos() {
        // given
        User user = new User(UUID.randomUUID(), "Test User", "test", "pass");
        given(userRepository.findAll()).willReturn(List.of(user));

        // when
        List<UserResponseDto> result = userService.getAllUsers();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).login()).isEqualTo("test");
        assertThat(result.get(0).id()).isEqualTo(user.getId());
        then(userRepository).should().findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoUsers() {
        // given
        given(userRepository.findAll()).willReturn(Collections.emptyList());

        // when
        List<UserResponseDto> result = userService.getAllUsers();

        // then
        assertThat(result).isEmpty();
    }
    
    @Test
    void shouldGetUserById() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "Test", "test", "pass");
        given(userRepository.findById(id)).willReturn(Optional.of(user));
        
        UserResponseDto result = userService.getUserById(id.toString());
        
        assertThat(result.id()).isEqualTo(id);
    }
    
    @Test
    void shouldCreateUser() {
        UserCreateRequest request = new UserCreateRequest("Name", "login", "pass");
        User savedUser = new User(UUID.randomUUID(), "Name", "login", "encodedPass");
        
        given(userRepository.existsByLogin("login")).willReturn(false);
        given(passwordEncoder.encode("pass")).willReturn("encodedPass");
        given(userRepository.save(any(User.class))).willReturn(savedUser);
        
        UserResponseDto response = userService.createUser(request);
        
        assertThat(response.login()).isEqualTo("login");
        assertThat(response.name()).isEqualTo("Name");
    }

    @Test
    void shouldThrowWhenCreatingDuplicateLogin() {
        UserCreateRequest request = new UserCreateRequest("Name", "login", "pass");
        given(userRepository.existsByLogin("login")).willReturn(true);
        
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Login already exists");
    }
    
    @Test
    void shouldUpdateUser() {
        UUID id = UUID.randomUUID();
        UserUpdateRequest request = new UserUpdateRequest("New Name", null, null);
        User existingUser = new User(id, "Old Name", "login", "pass");
        User updatedUser = new User(id, "New Name", "login", "pass");

        given(userRepository.findById(id)).willReturn(Optional.of(existingUser));
        given(userRepository.save(existingUser)).willReturn(updatedUser);

        UserResponseDto response = userService.updateUser(id.toString(), request);
        
        assertThat(response.name()).isEqualTo("New Name");
    }

    @Test
    void shouldDeleteUser() {
        UUID id = UUID.randomUUID();
        given(userRepository.existsById(id)).willReturn(true);
        
        userService.deleteUser(id.toString());
        
        then(userRepository).should().deleteById(id);
    }
}
