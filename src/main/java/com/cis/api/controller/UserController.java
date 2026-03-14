package com.cis.api.controller;

import com.cis.api.dto.UserResponseDto;
import com.cis.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for User Management API.
 * Provides endpoints for accessing user data.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Retrieves the full list of users.
     * US 1.1.1: Retrieve full list of users.
     *
     * @return 200 OK with List of UserResponseDto (no password)
     */
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Retrieves a specific user by ID.
     * US 1.1.2: Retrieve specific user by ID.
     *
     * @param id UUID of the user
     * @return 200 OK with UserResponseDto if found, 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
