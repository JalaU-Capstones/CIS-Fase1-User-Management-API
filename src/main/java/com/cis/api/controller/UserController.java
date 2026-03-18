package com.cis.api.controller;

import com.cis.api.dto.UserCreateRequest;
import com.cis.api.dto.UserResponseDto;
import com.cis.api.dto.UserUpdateRequest;
import com.cis.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * Retrieves a user by ID.
     * US 1.1.2: Retrieve specific user by ID.
     *
     * @param id User ID
     * @return 200 OK with UserResponseDto
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Creates a new user.
     * US 1.2.1: Create a new user.
     * Requires Authentication.
     *
     * @param request User creation data
     * @return 201 Created with UserResponseDto
     */
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody @Valid UserCreateRequest request) {
        return new ResponseEntity<>(userService.createUser(request), HttpStatus.CREATED);
    }

    /**
     * Updates an existing user.
     * US 1.3.1: Update a user by ID.
     * Requires Authentication.
     *
     * @param id User ID
     * @param request User update data
     * @return 200 OK with UserResponseDto
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable String id, @RequestBody @Valid UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    /**
     * Deletes a user by ID.
     * US 1.4.1: Delete a user by ID.
     * Requires Authentication.
     *
     * @param id User ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
