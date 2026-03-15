package com.cis.api.controller;

import com.cis.api.dto.UserRequestDto;
import com.cis.api.dto.UserResponseDto;
import com.cis.api.exception.GlobalExceptionHandler;
import com.cis.api.exception.ResourceNotFoundException;
import com.cis.api.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Configure MockMvc with a global exception advice
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        // given
        List<UserResponseDto> users = List.of(
                new UserResponseDto(UUID.randomUUID(), "John Doe", "jdoe"),
                new UserResponseDto(UUID.randomUUID(), "Jane Smith", "jsmith")
        );
        given(userService.getAllUsers()).willReturn(users);

        // when & then
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].login").value("jsmith"));
    }

    @Test
    void createUser_WithValidData_ShouldReturn201() throws Exception {
        // given
        UserRequestDto request = new UserRequestDto("Juan Pérez", "jperez", "123456");
        UUID userId = UUID.randomUUID();
        UserResponseDto response = new UserResponseDto(userId, "Juan Pérez", "jperez");

        given(userService.createUser(any(UserRequestDto.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Juan Pérez"))
                .andExpect(jsonPath("$.login").value("jperez"));
    }

    @Test
    void createUser_WithInvalidData_ShouldReturn400() throws Exception {
        // given
        UserRequestDto invalidRequest = new UserRequestDto("", "jp", "123");

        // when & then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_WhenLoginExists_ShouldReturn400() throws Exception {  // Cambiado a 400
        // given
        UserRequestDto request = new UserRequestDto("Juan Pérez", "jperez", "123456");

        given(userService.createUser(any(UserRequestDto.class)))
                .willThrow(new RuntimeException("Login already exists: jperez"));

        // when & then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());  // Ahora espera 400
    }
    /**
     * Verifies that GET /api/v1/users/{id} returns HTTP 200
     * and the correct user JSON when the user exists.
     * MockMvc simulates the HTTP call — no real server needed.
     */
    @Test
    void shouldReturnOkAndUserWhenExists() throws Exception {
        // given
        UUID id = UUID.randomUUID();
        UserResponseDto userDto = new UserResponseDto(id, "Paula", "pmartin");
        given(userService.getUserById(id.toString())).willReturn(userDto);

        // when/then
        mockMvc.perform(get("/api/v1/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Paula"))
                .andExpect(jsonPath("$.login").value("pmartin"));
    }

    /**
     * Verifies that GET /api/v1/users/{id} returns HTTP 404
     * when the user does not exist.
     * MockMvc simulates the HTTP call — no real server needed.
     */
    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        // given
        UUID id = UUID.randomUUID();
        given(userService.getUserById(id.toString()))
                .willThrow(new ResourceNotFoundException("User not found with id: " + id));

        // when/then
        mockMvc.perform(get("/api/v1/users/" + id))
                .andExpect(status().isNotFound());
    }

    // ===== TESTS Of Update (US 1.3.1) =====

    /**
     * Verifies that PUT /api/v1/users/{id} returns HTTP 200
     * and the updated user JSON when the user exists and data is valid.
     */
    @Test
    void updateUser_WithValidIdAndBody_ShouldReturn200() throws Exception {
        // given
        UUID id = UUID.randomUUID();
        UserRequestDto request = new UserRequestDto("Juan Actualizado", "jupdated", "newpass123");
        UserResponseDto response = new UserResponseDto(id, "Juan Actualizado", "jupdated");

        given(userService.updateUser(eq(id.toString()), any(UserRequestDto.class))).willReturn(response);

        // when & then
        mockMvc.perform(put("/api/v1/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Juan Actualizado"))
                .andExpect(jsonPath("$.login").value("jupdated"));
    }

    @Test
    void updateUser_WithNonExistentId_ShouldReturn404() throws Exception {
        // given
        UUID id = UUID.randomUUID();
        UserRequestDto request = new UserRequestDto("Juan", "juanv", "123456");

        given(userService.updateUser(eq(id.toString()), any(UserRequestDto.class)))
                .willThrow(new ResourceNotFoundException("User not found with id: " + id));

        // when & then
        mockMvc.perform(put("/api/v1/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
