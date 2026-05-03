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

        AuthResponse.AuthResponseBuilder builder = AuthResponse.builder()
                .token("token")
                .message("message");
        AuthResponse responseFromBuilder = builder.build();
        assertThat(responseFromBuilder.token()).isEqualTo("token");
        assertThat(responseFromBuilder.message()).isEqualTo("message");
        assertThat(builder.toString()).isNotEmpty();
        assertThat(responseFromBuilder.toString()).isNotEmpty();
    }

    @Test
    void testUserRequestDto() {
        UserRequestDto dto = new UserRequestDto("Name", "login", "pass");
        assertThat(dto.name()).isEqualTo("Name");
        assertThat(dto.login()).isEqualTo("login");
        assertThat(dto.password()).isEqualTo("pass");

        UserRequestDto.UserRequestDtoBuilder builder = UserRequestDto.builder()
                .name("Name")
                .login("login")
                .password("pass");
        UserRequestDto dtoFromBuilder = builder.build();
        assertThat(dtoFromBuilder.name()).isEqualTo("Name");
        assertThat(dtoFromBuilder.login()).isEqualTo("login");
        assertThat(dtoFromBuilder.password()).isEqualTo("pass");
        assertThat(builder.toString()).isNotEmpty();
        assertThat(dtoFromBuilder.toString()).isNotEmpty();
    }

    @Test
    void testUserResponseDto() {
        UUID id = UUID.randomUUID();
        UserResponseDto dto = new UserResponseDto(id, "Name", "login");
        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.name()).isEqualTo("Name");
        assertThat(dto.login()).isEqualTo("login");

        // Test equals, hashCode, toString for coverage
        UserResponseDto dto2 = new UserResponseDto(id, "Name", "login");
        assertThat(dto).isEqualTo(dto2);
        assertThat(dto.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto.toString()).contains(id.toString());
    }

    @Test
    void testAuthRequestExtra() {
        AuthRequest req1 = new AuthRequest("u", "p");
        AuthRequest req2 = new AuthRequest("u", "p");
        assertThat(req1).isEqualTo(req2);
        assertThat(req1.hashCode()).isEqualTo(req2.hashCode());
        assertThat(req1.toString()).contains("u");
    }

    @Test
    void testUserRequestDtoExtra() {
        UserRequestDto dto1 = new UserRequestDto("n", "l", "p");
        UserRequestDto dto2 = new UserRequestDto("n", "l", "p");
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.toString()).contains("n");
    }
}
