package com.cis.api.service;

import com.cis.api.dto.UserResponseDto;
import com.cis.api.model.User;
import com.cis.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    private UserResponseDto mapToDto(User user) {
        return new UserResponseDto(
            user.getId(),
            user.getName(),
            user.getLogin()
        );
    }
}
