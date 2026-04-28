package com.cis.api.service;

import com.cis.api.dto.UserMapper;
import com.cis.api.dto.UserRequestDto;
import com.cis.api.dto.UserResponseDto;
import com.cis.api.exception.ResourceNotFoundException;
import com.cis.api.model.User;
import com.cis.api.repository.UserPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Profile("!migrate")
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserPersistencePort userPersistencePort;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponseDto> getAllUsers() {
        return userPersistencePort.findAll().stream()
                .map(UserMapper::toResponseDto)
                .toList();
    }

    public UserResponseDto getUserById(String id) {
        return userPersistencePort.findById(UUID.fromString(id))
                .map(UserMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional
    public UserResponseDto createUser(UserRequestDto request) {
        validateLoginUniqueness(request.login());

        User user = UserMapper.toEntity(request);
        user.setId(UUID.randomUUID());
        user.setPassword(passwordEncoder.encode(request.password()));

        return UserMapper.toResponseDto(userPersistencePort.save(user));
    }

    @Transactional
    public UserResponseDto updateUser(String id, UserRequestDto request) {
        UUID uuid = UUID.fromString(id);
        User user = findUserById(uuid);

        checkOwnership(user);

        if (!user.getLogin().equals(request.login())) {
            validateLoginUniqueness(request.login(), uuid);
        }

        user.setName(request.name());
        user.setLogin(request.login());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        return UserMapper.toResponseDto(userPersistencePort.save(user));
    }

    @Transactional(readOnly = false)
    public void deleteUser(String id) {
        UUID uuid = UUID.fromString(id);
        User user = findUserById(uuid);

        checkOwnership(user);

        userPersistencePort.deleteUserAndRelatedData(uuid);
    }

    private User findUserById(UUID id) {
        return userPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private void checkOwnership(User user) {
        String currentUserLogin = getCurrentUserLogin();
        if (!user.getLogin().equals(currentUserLogin)) {
            throw new AccessDeniedException("You can only modify your own user record.");
        }
    }

    private String getCurrentUserLogin() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }

    private void validateLoginUniqueness(String login) {
        if (userPersistencePort.existsByLogin(login)) {
            throw new IllegalArgumentException("Login already exists: " + login);
        }
    }

    private void validateLoginUniqueness(String login, UUID userId) {
        if (userPersistencePort.existsByLoginAndIdNot(login, userId)) {
            throw new IllegalArgumentException("Login already exists: " + login);
        }
    }
}
