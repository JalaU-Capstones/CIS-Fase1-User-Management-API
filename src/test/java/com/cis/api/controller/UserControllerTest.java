package com.cis.api.controller;

import com.cis.api.dto.UserRequestDto;
import com.cis.api.dto.UserResponseDto;
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
}

/**
 * Global exception handler for tests
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
}