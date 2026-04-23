package com.cis.api.controller;

import com.cis.api.config.ApplicationConfig;
import com.cis.api.config.SecurityConfig;
import com.cis.api.dto.UserRequestDto;
import com.cis.api.dto.UserResponseDto;
import com.cis.api.exception.CustomAuthenticationEntryPoint;
import com.cis.api.exception.ResourceNotFoundException;
import com.cis.api.repository.MongoPersistencePort;
import com.cis.api.security.JwtAuthenticationFilter;
import com.cis.api.security.JwtService;
import com.cis.api.service.CustomUserDetailsService;
import com.cis.api.service.MongoUserService;
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

@WebMvcTest(UserV2Controller.class)
@Import({SecurityConfig.class, ApplicationConfig.class, JwtAuthenticationFilter.class, CustomAuthenticationEntryPoint.class})
@TestPropertySource(properties = {
        "application-properties.jwt.secret-key=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
        "application-properties.jwt.expiration-time=864000000"
})
class UserV2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MongoUserService mongoUserService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private MongoPersistencePort mongoPersistencePort;

    @Test
    void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        List<UserResponseDto> users = List.of(
                new UserResponseDto(UUID.randomUUID(), "Mongo John", "mjdoe")
        );
        given(mongoUserService.getAllUsers()).willReturn(users);

        mockMvc.perform(get("/api/v2/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Mongo John"));
    }

    @Test
    @WithMockUser
    void createUser_WithValidData_ShouldReturn201() throws Exception {
        UserRequestDto request = new UserRequestDto("Mongo User", "muser", "password");
        UUID userId = UUID.randomUUID();
        UserResponseDto response = new UserResponseDto(userId, "Mongo User", "muser");

        given(mongoUserService.createUser(any(UserRequestDto.class))).willReturn(response);

        mockMvc.perform(post("/api/v2/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value("muser"));
    }

    @Test
    @WithMockUser
    void createUser_WithInvalidData_ShouldReturn400() throws Exception {
        UserRequestDto invalidRequest = new UserRequestDto("", "mp", "123");

        mockMvc.perform(post("/api/v2/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_ShouldReturnUserWhenExists() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponseDto userDto = new UserResponseDto(id, "Mongo Paula", "mpmartin");
        given(mongoUserService.getUserById(id.toString())).willReturn(userDto);

        mockMvc.perform(get("/api/v2/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mongo Paula"))
                .andExpect(jsonPath("$.login").value("mpmartin"));
    }

    @Test
    void getUserById_ShouldReturnNotFoundWhenDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        given(mongoUserService.getUserById(id.toString()))
                .willThrow(new ResourceNotFoundException("User not found in MongoDB with id: " + id));

        mockMvc.perform(get("/api/v2/users/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void updateUser_WithValidIdAndBody_ShouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        UserRequestDto request = new UserRequestDto("Mongo Updated", "mupdated", "newpass123");
        UserResponseDto response = new UserResponseDto(id, "Mongo Updated", "mupdated");

        given(mongoUserService.updateUser(eq(id.toString()), any(UserRequestDto.class))).willReturn(response);

        mockMvc.perform(put("/api/v2/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mongo Updated"))
                .andExpect(jsonPath("$.login").value("mupdated"));
    }

    @Test
    @WithMockUser
    void deleteUser_WithExistingId_ShouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        
        mockMvc.perform(delete("/api/v2/users/" + id))
                .andExpect(status().isOk())
                .andExpect(content().string("User has been successfully deleted from MongoDB."));
    }

    @Test
    @WithMockUser
    void deleteUser_WithNonExistentId_ShouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("User not found in MongoDB with id: " + id))
                .when(mongoUserService).deleteUser(id.toString());

        mockMvc.perform(delete("/api/v2/users/" + id))
                .andExpect(status().isNotFound());
    }
}
