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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
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
    private UserService userService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }

        @Bean
        public JwtService jwtService() {
            return Mockito.mock(JwtService.class);
        }

        @Bean
        public CustomUserDetailsService customUserDetailsService() {
            return Mockito.mock(CustomUserDetailsService.class);
        }
    }

    @Test
    void shouldAllowPublicAccessToGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingUserWithEmptyBody() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAllowPublicAccessToCreateUser() throws Exception {
        UserResponseDto response = new UserResponseDto(UUID.randomUUID(), "Test", "test");
        given(userService.createUser(any(UserRequestDto.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"login\":\"test\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldDenyUnauthenticatedAccessToUpdateUser() throws Exception {
        mockMvc.perform(put("/api/v1/users/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void shouldAllowAuthenticatedAccessToUpdateUser() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponseDto response = new UserResponseDto(id, "Test", "test");
        given(userService.updateUser(eq(id.toString()), any(UserRequestDto.class))).willReturn(response);

        mockMvc.perform(put("/api/v1/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"login\":\"test\",\"password\":\"password123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyUnauthenticatedAccessToDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void shouldAllowAuthenticatedAccessToDeleteUser() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/users/" + id))
                .andExpect(status().isNoContent());
    }
}
