package com.cis.api.controller;

import com.cis.api.config.AccessLevelProperties;
import com.cis.api.config.ApplicationConfig;
import com.cis.api.config.SecurityConfig;
import com.cis.api.dto.AuthRequest;
import com.cis.api.dto.AuthResponse;
import com.cis.api.exception.CustomAuthenticationEntryPoint;
import com.cis.api.security.JwtAuthenticationFilter;
import com.cis.api.security.JwtService;
import com.cis.api.service.AuthenticationService;
import com.cis.api.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@Import({SecurityConfig.class, ApplicationConfig.class, JwtAuthenticationFilter.class, CustomAuthenticationEntryPoint.class,
        AccessLevelProperties.class})
@TestPropertySource(properties = {
        "application-properties.jwt.secret-key=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
        "application-properties.jwt.expiration-time=864000000"
})
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldReturnTokenOnValidLogin() throws Exception {
        AuthRequest request = new AuthRequest("user", "pass");
        AuthResponse response = new AuthResponse("token123");

        given(authenticationService.authenticate(request)).willReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"user\", \"password\":\"pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token123"));
    }
}
