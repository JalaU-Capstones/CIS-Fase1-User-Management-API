package com.cis.api.service;

import com.cis.api.dto.UserMapper;
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
import org.springframework.security.crypto.password.PasswordEncoder;

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
class MongoUserServiceTest {

    @Mock
    private MongoPersistencePort mongoPersistencePort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

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
        UserResponseDto userResponseDto = new UserResponseDto(user.getId(), user.getName(), user.getLogin());
        given(mongoPersistencePort.findAll()).willReturn(List.of(user));
        given(userMapper.toDto(user)).willReturn(userResponseDto);

        List<UserResponseDto> result = mongoUserService.getAllUsers();

        assertThat(result).hasSize(1);
        then(mongoPersistencePort).should().findAll();
    }

    @Test
    void shouldGetUserById() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "Mongo", "mongo", "pass");
        UserResponseDto userResponseDto = new UserResponseDto(user.getId(), user.getName(), user.getLogin());
        given(mongoPersistencePort.findById(id)).willReturn(Optional.of(user));
        given(userMapper.toDto(user)).willReturn(userResponseDto);

        UserResponseDto result = mongoUserService.getUserById(id.toString());

        assertThat(result.id()).isEqualTo(id);
    }

    @Test
    void shouldCreateUser() {
        UserRequestDto request = new UserRequestDto("Name", "mongo", "pass");
        User savedUser = new User(UUID.randomUUID(), "Name", "mongo", "encodedPass");
        UserResponseDto userResponseDto = new UserResponseDto(savedUser.getId(), savedUser.getName(), savedUser.getLogin());

        given(mongoPersistencePort.existsByLogin("mongo")).willReturn(false);
        given(passwordEncoder.encode("pass")).willReturn("encodedPass");
        given(mongoPersistencePort.save(any(User.class))).willReturn(savedUser);
        given(userMapper.toDto(savedUser)).willReturn(userResponseDto);

        UserResponseDto response = mongoUserService.createUser(request);

        assertThat(response.login()).isEqualTo("mongo");
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
}
