package com.cis.api.controller;

import com.cis.api.dto.UserRequestDto;
import com.cis.api.dto.UserResponseDto;
import com.cis.api.exception.ErrorResponse;
import com.cis.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Users", description = "Operations related to user management")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all users", description = "This method brings all users from the database.")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully, returns empty list if none exist")
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Get user by Id", description = "This method retrieves a specific user by their ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDto.class),
                            examples = @ExampleObject(value = "{\"id\": \"550e8400-e29b-41d4-a716-446655440000\", \"name\": \"John Doe\", \"username\": \"johndoe\"}"))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"error\": \"Not Found\", \"message\": \"User not found with id: 550e8400-e29b-41d4-a716-446655440000\"}")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@Parameter(description = "UUID of the user", example = "550e8400-e29b-41d4-a716-446655440000")
                                                           @PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Create a user", description = "Create a new user in the database")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDto.class),
                            examples = @ExampleObject(value = "{\"id\": \"550e8400-e29b-41d4-a716-446655440000\", \"name\": \"John Doe\", \"username\": \"johndoe\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid request body, validation failed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 400, \"error\": \"Bad Request\", \"message\": \"Validation failed\", \"details\": {\"username\": \"must not be empty\"}}")))
    })
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody @Valid UserRequestDto request) {
        return new ResponseEntity<>(userService.createUser(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Update a user", description = "This method updates the name, username, and/or password of an existing user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDto.class),
                            examples = @ExampleObject(value = "{\"id\": \"550e8400-e29b-41d4-a716-446655440000\", \"name\": \"John Updated\", \"username\": \"johnupdated\"}"))),
            @ApiResponse(responseCode = "400", description = "Invalid request body, validation failed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 400, \"error\": \"Bad Request\", \"message\": \"Validation failed\", \"details\": {\"name\": \"must not be blank\"}}"))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"error\": \"Not Found\", \"message\": \"User not found with id: 550e8400-e29b-41d4-a716-446655440000\"}")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @Parameter(description = "UUID of the user", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id,
            @RequestBody @Valid UserRequestDto request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @Operation(summary = "Delete a user", description = "This method deletes an existing user by their Id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User successfully deleted.",
                    content = @Content(mediaType = "text/plain",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "User and all related topics, ideas, and votes have been successfully deleted."))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 404, \"error\": \"Not Found\", \"message\": \"User not found with id: 550e8400-e29b-41d4-a716-446655440000\"}"))),
            @ApiResponse(responseCode = "403", description = "Forbidden: You can only delete your own user record.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"status\": 403, \"error\": \"Forbidden\", \"message\": \"Access Denied\"}")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@Parameter(description = "UUID of the user", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User and all related topics, ideas, and votes have been successfully deleted.");
    }
}
