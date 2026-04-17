package com.cis.api.controller;

import com.cis.api.dto.UserRequestDto;
import com.cis.api.dto.UserResponseDto;
import com.cis.api.exception.ErrorResponse;
import com.cis.api.service.MongoUserService;
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

/**
 * Controller for user management V2 (MongoDB).
 */
@Tag(name = "Users V2", description = "Operations related to user management using MongoDB")
@RestController
@RequestMapping("/api/v2/users")
@RequiredArgsConstructor
public class UserV2Controller {

    private final MongoUserService userService;

    @Operation(summary = "Get all users (V2)", description = "This method brings all users from the MongoDB database.")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully, returns empty list if none exist")
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Get user by Id (V2)", description = "This method retrieves a specific user by their ID from MongoDB.")
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

    @Operation(summary = "Create a user (V2)", description = "Create a new user in the MongoDB database")
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

    @Operation(summary = "Update a user (V2)", description = "This method updates a user in MongoDB")
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

    @Operation(summary = "Delete a user (V2)", description = "This method deletes a user from MongoDB")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User has been successfully deleted.",
                    content = @Content(mediaType = "text/plain",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "User has been successfully deleted from MongoDB."))),
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
        return ResponseEntity.ok("User has been successfully deleted from MongoDB.");
    }
}
