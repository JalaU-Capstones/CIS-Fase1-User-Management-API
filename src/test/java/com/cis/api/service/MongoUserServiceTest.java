package com.cis.api.service;

import com.cis.api.dto.UserRequestDto;
import com.cis.api.dto.UserResponseDto;
import com.cis.api.exception.ResourceNotFoundException;
import com.cis.api.model.User;
import com.cis.api.repository.MongoPersistencePort;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class MongoUserServiceTest {

    @Mock
    private MongoPersistencePort mongoPersistencePort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MongoUserService mongoUserService;

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
        User user = new User(UUID.randomUUID(), "Mongo User", "mongo", "pass");
        given(mongoPersistencePort.findAll()).willReturn(List.of(user));

        List<UserResponseDto> result = mongoUserService.getAllUsers();

        assertThat(result).hasSize(1);
        then(mongoPersistencePort).should().findAll();
    }

    @Test
    void shouldGetUserById() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "Mongo", "mongo", "pass");
        given(mongoPersistencePort.findById(id)).willReturn(Optional.of(user));

        UserResponseDto result = mongoUserService.getUserById(id.toString());

        assertThat(result.id()).isEqualTo(id);
    }

    @Test
    void shouldThrowExceptionWhenUserByIdNotFound() {
        UUID id = UUID.randomUUID();
        given(mongoPersistencePort.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> mongoUserService.getUserById(id.toString()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldCreateUser() {
        UserRequestDto request = new UserRequestDto("Name", "mongo", "pass");
        User savedUser = new User(UUID.randomUUID(), "Name", "mongo", "encodedPass");

        given(mongoPersistencePort.existsByLogin("mongo")).willReturn(false);
        given(passwordEncoder.encode("pass")).willReturn("encodedPass");
        given(mongoPersistencePort.save(any(User.class))).willReturn(savedUser);

        UserResponseDto response = mongoUserService.createUser(request);

        assertThat(response.login()).isEqualTo("mongo");
    }

    @Test
    void shouldThrowExceptionWhenCreatingUserWithExistingLogin() {
        UserRequestDto request = new UserRequestDto("Name", "existing", "pass");
        given(mongoPersistencePort.existsByLogin("existing")).willReturn(true);

        assertThatThrownBy(() -> mongoUserService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Login already exists");
    }

    @Test
    void shouldUpdateUserWhenOwner() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "Old Name", "owner", "encodedOldPass");
        UserRequestDto request = new UserRequestDto("New Name", "owner", "newPass");
        User updatedUser = new User(id, "New Name", "owner", "encodedNewPass");

        mockAuthentication("owner");
        given(mongoPersistencePort.findById(id)).willReturn(Optional.of(user));
        given(passwordEncoder.encode("newPass")).willReturn("encodedNewPass");
        given(mongoPersistencePort.save(any(User.class))).willReturn(updatedUser);

        UserResponseDto result = mongoUserService.updateUser(id.toString(), request);

        assertThat(result.name()).isEqualTo("New Name");
        then(mongoPersistencePort).should().save(any(User.class));
    }

    @Test
    void shouldUpdateUserWhenOwnerAndLoginChanges() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "Name", "oldLogin", "pass");
        UserRequestDto request = new UserRequestDto("Name", "newLogin", null);
        User updatedUser = new User(id, "Name", "newLogin", "pass");

        mockAuthentication("oldLogin");
        given(mongoPersistencePort.findById(id)).willReturn(Optional.of(user));
        given(mongoPersistencePort.existsByLoginAndIdNot("newLogin", id)).willReturn(false);
        given(mongoPersistencePort.save(any(User.class))).willReturn(updatedUser);

        UserResponseDto result = mongoUserService.updateUser(id.toString(), request);

        assertThat(result.login()).isEqualTo("newLogin");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithExistingLoginForAnotherUser() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "Name", "oldLogin", "pass");
        UserRequestDto request = new UserRequestDto("Name", "takenLogin", null);

        mockAuthentication("oldLogin");
        given(mongoPersistencePort.findById(id)).willReturn(Optional.of(user));
        given(mongoPersistencePort.existsByLoginAndIdNot("takenLogin", id)).willReturn(true);

        assertThatThrownBy(() -> mongoUserService.updateUser(id.toString(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Login already exists");
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        UUID id = UUID.randomUUID();
        UserRequestDto request = new UserRequestDto("Name", "login", "pass");
        given(mongoPersistencePort.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> mongoUserService.updateUser(id.toString(), request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingAnotherUserRecord() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "Other User", "other", "pass");
        UserRequestDto request = new UserRequestDto("New Name", "other", "pass");

        mockAuthentication("me");
        given(mongoPersistencePort.findById(id)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> mongoUserService.updateUser(id.toString(), request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You can only modify your own user record");
    }

    @Test
    void shouldGetCurrentUserLoginFromUserDetails() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "Test", "user123", "pass");
        UserRequestDto request = new UserRequestDto("New Name", "user123", null);

        UserDetails userDetails = mock(UserDetails.class);
        given(userDetails.getUsername()).willReturn("user123");
        mockAuthentication(userDetails);

        given(mongoPersistencePort.findById(id)).willReturn(Optional.of(user));
        given(mongoPersistencePort.save(any(User.class))).willReturn(user);

        mongoUserService.updateUser(id.toString(), request);
        
        then(userDetails).should().getUsername();
    }

    @Test
    void shouldDeleteUserWhenOwner() {
        UUID id = UUID.randomUUID();
        String idStr = id.toString();
        String login = "owner";
        User user = new User(id, "Test", login, "pass");

        mockAuthentication(login);
        given(mongoPersistencePort.findById(id)).willReturn(Optional.of(user));

        mongoUserService.deleteUser(idStr);

        then(mongoPersistencePort).should().deleteUserAndRelatedData(id);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentUser() {
        UUID id = UUID.randomUUID();
        given(mongoPersistencePort.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> mongoUserService.deleteUser(id.toString()))
                .isInstanceOf(ResourceNotFoundException.class);

        then(mongoPersistencePort).should(never()).deleteUserAndRelatedData(any());
    }

    @Test
    void shouldThrowExceptionWhenDeletingAnotherUserRecord() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "Other User", "other", "pass");

        mockAuthentication("me");
        given(mongoPersistencePort.findById(id)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> mongoUserService.deleteUser(id.toString()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You can only modify your own user record");

        then(mongoPersistencePort).should(never()).deleteUserAndRelatedData(any());
    }
}
