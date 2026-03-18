package com.cis.api.controller;

import com.cis.api.config.SecurityConfig;
import com.cis.api.dto.UserCreateRequest;
import com.cis.api.dto.UserResponseDto;
import com.cis.api.dto.UserUpdateRequest;
import com.cis.api.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    // We need to mock these beans because they are required by SecurityConfig
    @MockitoBean
    private com.cis.api.security.JwtService jwtService;
    @MockitoBean
    private com.cis.api.service.CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldAllowPublicAccessToGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyUnauthenticatedAccessToCreateUser() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andExpect(status().isForbidden()); // Or 401 depending on how filter chain is hit
    }

    @Test
    @WithMockUser
    void shouldAllowAuthenticatedAccessToCreateUser() throws Exception {
        UserResponseDto response = new UserResponseDto(UUID.randomUUID(), "Test", "test");
        given(userService.createUser(any(UserCreateRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"login\":\"test\",\"password\":\"pass\"}")
                        .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldDenyUnauthenticatedAccessToUpdateUser() throws Exception {
        mockMvc.perform(put("/api/v1/users/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldAllowAuthenticatedAccessToUpdateUser() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponseDto response = new UserResponseDto(id, "Test", "test");
        given(userService.updateUser(eq(id.toString()), any(UserUpdateRequest.class))).willReturn(response);

        mockMvc.perform(put("/api/v1/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\"}")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyUnauthenticatedAccessToDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + UUID.randomUUID())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void shouldAllowAuthenticatedAccessToDeleteUser() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/users/" + id)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
