package com.cis.api.controller;

import com.cis.api.config.ApplicationConfig;
import com.cis.api.config.SecurityConfig;
import com.cis.api.dto.UserRequestDto;
import com.cis.api.dto.UserResponseDto;
import com.cis.api.exception.CustomAuthenticationEntryPoint;
import com.cis.api.exception.ResourceNotFoundException;
import com.cis.api.security.JwtAuthenticationFilter;
import com.cis.api.security.JwtService;
import com.cis.api.service.CustomUserDetailsService;
import com.cis.api.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, ApplicationConfig.class, JwtAuthenticationFilter.class, CustomAuthenticationEntryPoint.class})
@TestPropertySource(properties = {
        "application-properties.jwt.secret-key=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
        "application-properties.jwt.expiration-time=864000000"
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        List<UserResponseDto> users = List.of(
                new UserResponseDto(UUID.randomUUID(), "John Doe", "jdoe"),
                new UserResponseDto(UUID.randomUUID(), "Jane Smith", "jsmith")
        );
        given(userService.getAllUsers()).willReturn(users);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].login").value("jsmith"));
    }

    @Test
    @WithMockUser
    void createUser_WithValidData_ShouldReturn201() throws Exception {
        UserRequestDto request = new UserRequestDto("Juan Pérez", "jperez", "123456");
        UUID userId = UUID.randomUUID();
        UserResponseDto response = new UserResponseDto(userId, "Juan Pérez", "jperez");

        given(userService.createUser(any(UserRequestDto.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Juan Pérez"))
                .andExpect(jsonPath("$.login").value("jperez"));
    }

    @Test
    @WithMockUser
    void createUser_WithInvalidData_ShouldReturn400() throws Exception {
        UserRequestDto invalidRequest = new UserRequestDto("", "jp", "123");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnOkAndUserWhenExists() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponseDto userDto = new UserResponseDto(id, "Paula", "pmartin");
        given(userService.getUserById(id.toString())).willReturn(userDto);

        mockMvc.perform(get("/api/v1/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Paula"))
                .andExpect(jsonPath("$.login").value("pmartin"));
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        given(userService.getUserById(id.toString()))
                .willThrow(new ResourceNotFoundException("User not found with id: " + id));

        mockMvc.perform(get("/api/v1/users/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateUser_WithValidIdAndBody_ShouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        UserRequestDto request = new UserRequestDto("Juan Actualizado", "jupdated", "newpass123");
        UserResponseDto response = new UserResponseDto(id, "Juan Actualizado", "jupdated");

        given(userService.updateUser(eq(id.toString()), any(UserRequestDto.class))).willReturn(response);

        mockMvc.perform(put("/api/v1/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Juan Actualizado"))
                .andExpect(jsonPath("$.login").value("jupdated"));
    }

    @Test
    @WithMockUser
    void updateUser_WithNonExistentId_ShouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        UserRequestDto request = new UserRequestDto("Juan", "juanv", "123456");

        given(userService.updateUser(eq(id.toString()), any(UserRequestDto.class)))
                .willThrow(new ResourceNotFoundException("User not found with id: " + id));

        mockMvc.perform(put("/api/v1/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deleteUser_WithExistingId_ShouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        
        mockMvc.perform(delete("/api/v1/users/" + id))
                .andExpect(status().isOk())
                .andExpect(content().string("User and all related topics, ideas, and votes have been successfully deleted."));
    }

    @Test
    @WithMockUser
    void deleteUser_WithNonExistentId_ShouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("User not found with id: " + id))
                .when(userService).deleteUser(id.toString());

        mockMvc.perform(delete("/api/v1/users/" + id))
                .andExpect(status().isNotFound());
    }
}
