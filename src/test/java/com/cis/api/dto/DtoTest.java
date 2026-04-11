package com.cis.api.dto;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DtoTest {

    @Test
    void testAuthRequest() {
        AuthRequest request = new AuthRequest("login", "pass");
        assertThat(request.login()).isEqualTo("login");
        assertThat(request.password()).isEqualTo("pass");
    }

    @Test
    void testAuthResponse() {
        AuthResponse response = new AuthResponse("token", "message");
        assertThat(response.token()).isEqualTo("token");
        assertThat(response.message()).isEqualTo("message");

        AuthResponse responseFromBuilder = AuthResponse.builder()
                .token("token")
                .message("message")
                .build();
        assertThat(responseFromBuilder.token()).isEqualTo("token");
        assertThat(responseFromBuilder.message()).isEqualTo("message");
    }

    @Test
    void testUserRequestDto() {
        UserRequestDto dto = new UserRequestDto("Name", "login", "pass");
        assertThat(dto.name()).isEqualTo("Name");
        assertThat(dto.login()).isEqualTo("login");
        assertThat(dto.password()).isEqualTo("pass");

        UserRequestDto dtoFromBuilder = UserRequestDto.builder()
                .name("Name")
                .login("login")
                .password("pass")
                .build();
        assertThat(dtoFromBuilder.name()).isEqualTo("Name");
        assertThat(dtoFromBuilder.login()).isEqualTo("login");
        assertThat(dtoFromBuilder.password()).isEqualTo("pass");
    }

    @Test
    void testUserResponseDto() {
        UUID id = UUID.randomUUID();
        UserResponseDto dto = new UserResponseDto(id, "Name", "login");
        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.name()).isEqualTo("Name");
        assertThat(dto.login()).isEqualTo("login");
    }
}
