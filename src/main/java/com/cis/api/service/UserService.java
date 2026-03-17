package com.cis.api.service;

import com.cis.api.dto.UserRequestDto;
import com.cis.api.dto.UserResponseDto;
import com.cis.api.exception.ResourceNotFoundException;
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
     * Retrieves a specific user by ID.
     * US 1.1.2: Retrieve specific user by ID.
     *
     * @param id UUID of the user
     * @return UserResponseDto if found
     * @throws ResourceNotFoundException if user not found
     */
    public UserResponseDto getUserById(String id) {
        UUID uuid = UUID.fromString(id);
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToDto(user);
    }

    /**
     * Creates a new user.
     * US 1.2.1: Create a new user.
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

    /**
     * Updates an existing user by ID.
     * US 1.3.1: Update a user by ID.
     *
     * @param id          UUID of the user to update
     * @param userRequest The new data for the user
     * @return UserResponseDto of the updated user
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public UserResponseDto updateUser(String id, UserRequestDto userRequest) {
        UUID uuid = UUID.fromString(id);

        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!user.getLogin().equals(userRequest.login())
                && userRepository.existsByLoginAndIdNot(userRequest.login(), uuid)) {
            throw new RuntimeException("Login already exists: " + userRequest.login());
        }

        user.setName(userRequest.name());
        user.setLogin(userRequest.login());
        user.setPassword(userRequest.password());

        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    /**
     * Deletes a user by ID
     * US 1.4.1: Delete a user by ID
     *
     * @param id UUID of the user to delete
     * @throws ResourceNotFoundException if user not found
     * @throws IllegalArgumentException if ID format is invalid
     */
    @Transactional
    public void deleteUser(String id) {
        UUID uuid = UUID.fromString(id);

        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userRepository.delete(user);
    }
}