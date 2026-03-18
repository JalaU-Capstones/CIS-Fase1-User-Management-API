package com.cis.api.service;

import com.cis.api.dto.UserCreateRequest;
import com.cis.api.dto.UserResponseDto;
import com.cis.api.dto.UserUpdateRequest;
import com.cis.api.exception.ResourceNotFoundException;
import com.cis.api.model.User;
import com.cis.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

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
     * @param request DTO with user creation data
     * @return DTO of the created user
     */
    @Transactional
    public UserResponseDto createUser(UserCreateRequest request) {
        if (userRepository.existsByLogin(request.getLogin())) {
            throw new RuntimeException("Login already exists: " + request.getLogin());
        }
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName(request.getName());
        user.setLogin(request.getLogin());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Encode password
        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    /**
     * Updates an existing user.
     * @param id User ID
     * @param request DTO with user update data
     * @return DTO of the updated user
     */
    @Transactional
    public UserResponseDto updateUser(String id, UserUpdateRequest request) {
        UUID uuid = UUID.fromString(id);
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        if (request.getLogin() != null && !user.getLogin().equals(request.getLogin())
                && userRepository.existsByLoginAndIdNot(request.getLogin(), uuid)) {
            throw new RuntimeException("Login already exists: " + request.getLogin());
        }

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getLogin() != null) {
            user.setLogin(request.getLogin());
        }
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    /**
     * Deletes a user by ID.
     * @param id User ID
     */
    @Transactional
    public void deleteUser(String id) {
        UUID uuid = UUID.fromString(id);
        if (!userRepository.existsById(uuid)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(uuid);
    }

    private UserResponseDto mapToDto(User user) {
        return new UserResponseDto(
            user.getId(),
            user.getName(),
            user.getLogin()
        );
    }
}
