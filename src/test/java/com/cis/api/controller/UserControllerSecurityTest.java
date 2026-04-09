package com.cis.api.controller;

import com.cis.api.config.ApplicationConfig;
import com.cis.api.config.SecurityConfig;
import com.cis.api.dto.UserRequestDto;
import com.cis.api.dto.UserResponseDto;
import com.cis.api.exception.CustomAuthenticationEntryPoint;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, ApplicationConfig.class, JwtAuthenticationFilter.class, CustomAuthenticationEntryPoint.class})
@TestPropertySource(properties = {
        "application-properties.jwt.secret-key=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
        "application-properties.jwt.expiration-time=864000000"
})
class UserControllerSecurityTest {

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
    void shouldAllowPublicAccessToGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser")
    void shouldAllowAuthenticatedAccessToCreateUser() throws Exception {
        UserRequestDto request = new UserRequestDto("New User", "newuser", "password123");
        UserResponseDto response = new UserResponseDto(UUID.randomUUID(), "New User", "newuser");
        given(userService.createUser(any(UserRequestDto.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldDenyUnauthenticatedAccessToCreateUser() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"login\":\"test\",\"password\":\"password123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "owner")
    void shouldAllowAuthenticatedAccessToUpdateOwnUser() throws Exception {
        UUID id = UUID.randomUUID();
        UserRequestDto request = new UserRequestDto("Test", "owner", "password123");
        UserResponseDto response = new UserResponseDto(id, "Test", "owner");
        given(userService.updateUser(eq(id.toString()), any(UserRequestDto.class))).willReturn(response);

        mockMvc.perform(put("/api/v1/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "anotherUser")
    void shouldDenyAuthenticatedAccessToUpdateAnotherUser() throws Exception {
        UUID id = UUID.randomUUID();
        UserRequestDto request = new UserRequestDto("Test", "owner", "password123"); // Define request here
        doThrow(new AccessDeniedException("You can only modify your own user record."))
                .when(userService).updateUser(eq(id.toString()), any(UserRequestDto.class));

        mockMvc.perform(put("/api/v1/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDenyUnauthenticatedAccessToUpdateUser() throws Exception {
        mockMvc.perform(put("/api/v1/users/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "owner")
    void shouldAllowAuthenticatedAccessToDeleteOwnUser() throws Exception {
        UUID id = UUID.randomUUID();
        // No need to mock return value for void method, just ensure it doesn't throw
        mockMvc.perform(delete("/api/v1/users/" + id))
                .andExpect(status().isOk()); // Changed to isOk()
    }

    @Test
    @WithMockUser(username = "anotherUser")
    void shouldDenyAuthenticatedAccessToDeleteAnotherUser() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new AccessDeniedException("You can only modify your own user record."))
                .when(userService).deleteUser(eq(id.toString()));

        mockMvc.perform(delete("/api/v1/users/" + id))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDenyUnauthenticatedAccessToDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
