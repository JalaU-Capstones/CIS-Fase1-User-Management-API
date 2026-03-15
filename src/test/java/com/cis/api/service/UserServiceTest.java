package com.cis.api.service;

import com.cis.api.dto.UserRequestDto;
import com.cis.api.dto.UserResponseDto;
import com.cis.api.exception.ResourceNotFoundException;
import com.cis.api.model.User;
import com.cis.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    // ===== TESTS DE LECTURA - LISTA (US 1.1.1) =====

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

    // ===== TESTS DE LECTURA - POR ID (US 1.1.2) =====

    @Test
    void shouldReturnUserDtoWhenUserExists() {
        // given
        UUID id = UUID.randomUUID();
        User user = new User(id, "Paula", "pmartin", "pass");
        given(userRepository.findById(id)).willReturn(Optional.of(user));

        // when
        UserResponseDto result = userService.getUserById(id.toString());

        // then
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("Paula");
        assertThat(result.login()).isEqualTo("pmartin");
        assertThat(result).isNotNull();
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUserDoesNotExist() {
        // given
        UUID id = UUID.randomUUID();
        given(userRepository.findById(id)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> userService.getUserById(id.toString()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id:");
    }

    // ===== TESTS DE CREACIÓN (US 1.2.1) =====

    @Test
    void shouldCreateUserSuccessfully() {
        // given
        UserRequestDto request = new UserRequestDto("Juan Pérez", "jperez", "123456");
        User userToSave = new User();
        userToSave.setId(UUID.randomUUID());
        userToSave.setName(request.name());
        userToSave.setLogin(request.login());
        userToSave.setPassword(request.password());

        given(userRepository.existsByLogin(request.login())).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(userToSave);

        // when
        UserResponseDto result = userService.createUser(request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Juan Pérez");
        assertThat(result.login()).isEqualTo("jperez");
        assertThat(result.id()).isNotNull();

        then(userRepository).should().existsByLogin("jperez");
        then(userRepository).should().save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenLoginAlreadyExists() {
        // given
        UserRequestDto request = new UserRequestDto("Juan Pérez", "jperez", "123456");
        given(userRepository.existsByLogin(request.login())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Login already exists: jperez");

        then(userRepository).should().existsByLogin("jperez");
        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    void shouldGenerateValidUuidWhenCreatingUser() {
        // given
        UserRequestDto request = new UserRequestDto("Juan Pérez", "jperez", "123456");

        given(userRepository.existsByLogin(request.login())).willReturn(false);
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User userToSave = invocation.getArgument(0);
            userToSave.setId(UUID.randomUUID());
            return userToSave;
        });

        // when
        UserResponseDto result = userService.createUser(request);

        // then
        assertThat(result.id()).isNotNull();
        assertThat(result.id().toString()).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
    }

    // ===== TESTS of Update (US 1.3.1) =====

    @Test
    void shouldUpdateUserSuccessfully() {
        // given
        UUID id = UUID.randomUUID();
        User existingUser = new User(id, "Juan Viejo", "juanv", "oldpass");
        UserRequestDto request = new UserRequestDto("Juan Actualizado", "jupdated", "newpass123");
        User updatedUser = new User(id, "Juan Actualizado", "jupdated", "newpass123");

        given(userRepository.findById(id)).willReturn(Optional.of(existingUser));
        given(userRepository.existsByLoginAndIdNot("jupdated", id)).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(updatedUser);

        // when
        UserResponseDto result = userService.updateUser(id.toString(), request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("Juan Actualizado");
        assertThat(result.login()).isEqualTo("jupdated");

        then(userRepository).should().findById(id);
        then(userRepository).should().save(any(User.class));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUpdatingNonExistentUser() {
        // given
        UUID id = UUID.randomUUID();
        UserRequestDto request = new UserRequestDto("Juan", "juanv", "123456");

        given(userRepository.findById(id)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateUser(id.toString(), request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id:");

        then(userRepository).should().findById(id);
        then(userRepository).should(never()).save(any(User.class));
    }
}