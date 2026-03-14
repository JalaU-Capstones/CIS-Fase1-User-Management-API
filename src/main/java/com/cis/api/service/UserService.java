package com.cis.api.service;

import com.cis.api.dto.UserRequestDto;
import com.cis.api.dto.UserResponseDto;
import com.cis.api.model.User;
import com.cis.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for User-related business logic.
 * Encapsulates operations and converts entities to DTOs.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    /**
     * Retrieves all users from the database and converts them to DTOs.
     * Ensures passwords are not exposed.
     *
     * @return List of UserResponseDto
     */
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new user.
     *
     * @param userRequest The user data from the request
     * @return UserResponseDto of the created user
     * @throws RuntimeException if login already exists
     */
    @Transactional
    public UserResponseDto createUser(UserRequestDto userRequest) {
        // Check if login already exists (R2: legacy compatibility)
        if (userRepository.existsByLogin(userRequest.login())) {
            throw new RuntimeException("Login already exists: " + userRequest.login());
        }

        // Create new user with generated UUID
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName(userRequest.name());
        user.setLogin(userRequest.login());
        user.setPassword(userRequest.password());

        // Save to database
        User savedUser = userRepository.save(user);

        // Return response without password
        return mapToDto(savedUser);
    }

    private UserResponseDto mapToDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getLogin()
        );
    }
}