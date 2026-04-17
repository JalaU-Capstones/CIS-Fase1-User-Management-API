package com.cis.api.service;

import com.cis.api.dto.UserMapper;
import com.cis.api.dto.UserRequestDto;
import com.cis.api.dto.UserResponseDto;
import com.cis.api.exception.ResourceNotFoundException;
import com.cis.api.model.User;
import com.cis.api.repository.MongoPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MongoUserService {

    private final MongoPersistencePort mongoPersistencePort;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public List<UserResponseDto> getAllUsers() {
        return mongoPersistencePort.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    public UserResponseDto getUserById(String id) {
        return mongoPersistencePort.findById(UUID.fromString(id))
                .map(userMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public UserResponseDto createUser(UserRequestDto request) {
        validateLoginUniqueness(request.login());

        User user = new User();
        user.setId(UUID.randomUUID());
        userMapper.updateUserFromDto(request, user);
        user.setPassword(passwordEncoder.encode(request.password()));

        return userMapper.toDto(mongoPersistencePort.save(user));
    }

    public UserResponseDto updateUser(String id, UserRequestDto request) {
        UUID uuid = UUID.fromString(id);
        User user = findUserById(uuid);

        checkOwnership(user);

        if (!user.getLogin().equals(request.login())) {
            validateLoginUniqueness(request.login(), uuid);
        }

        userMapper.updateUserFromDto(request, user);
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        return userMapper.toDto(mongoPersistencePort.save(user));
    }

    public void deleteUser(String id) {
        UUID uuid = UUID.fromString(id);
        User user = findUserById(uuid);

        checkOwnership(user);

        mongoPersistencePort.deleteUserAndRelatedData(uuid);
    }

    private User findUserById(UUID id) {
        return mongoPersistencePort.findById(id)
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
        if (mongoPersistencePort.existsByLogin(login)) {
            throw new IllegalArgumentException("Login already exists: " + login);
        }
    }

    private void validateLoginUniqueness(String login, UUID userId) {
        if (mongoPersistencePort.existsByLoginAndIdNot(login, userId)) {
            throw new IllegalArgumentException("Login already exists: " + login);
        }
    }
}
