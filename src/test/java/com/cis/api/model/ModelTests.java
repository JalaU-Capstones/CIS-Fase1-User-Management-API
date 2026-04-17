package com.cis.api.model;

import com.cis.api.dto.AuthRequest;
import com.cis.api.dto.AuthResponse;
import com.cis.api.dto.UserMapper;
import com.cis.api.dto.UserRequestDto;
import com.cis.api.dto.UserResponseDto;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class ModelTests {

    @Test
    void userModelTest() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "Name", "login", "pass");
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getName()).isEqualTo("Name");
        assertThat(user.getLogin()).isEqualTo("login");
        assertThat(user.getPassword()).isEqualTo("pass");

        User emptyUser = new User();
        emptyUser.setId(id);
        emptyUser.setName("Name");
        emptyUser.setLogin("login");
        emptyUser.setPassword("pass");
        assertThat(emptyUser.getId()).isEqualTo(id);
        assertThat(emptyUser.getName()).isEqualTo("Name");
        assertThat(emptyUser.getLogin()).isEqualTo("login");
        assertThat(emptyUser.getPassword()).isEqualTo("pass");
    }

    @Test
    void mongoUserModelTest() {
        String id = UUID.randomUUID().toString();
        MongoUser user = new MongoUser(id, "Name", "login", "pass");
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getName()).isEqualTo("Name");
        assertThat(user.getLogin()).isEqualTo("login");
        assertThat(user.getPassword()).isEqualTo("pass");

        MongoUser emptyUser = new MongoUser();
        emptyUser.setId(id);
        emptyUser.setName("Name");
        emptyUser.setLogin("login");
        emptyUser.setPassword("pass");
        assertThat(emptyUser.getId()).isEqualTo(id);
        assertThat(emptyUser.getName()).isEqualTo("Name");
        assertThat(emptyUser.getLogin()).isEqualTo("login");
        assertThat(emptyUser.getPassword()).isEqualTo("pass");
    }

    @Test
    void authRequestTest() {
        AuthRequest request = new AuthRequest("user", "pass");
        assertThat(request.login()).isEqualTo("user");
        assertThat(request.password()).isEqualTo("pass");
    }

    @Test
    void authResponseTest() {
        AuthResponse response = AuthResponse.builder()
                .token("token")
                .message("success")
                .build();
        assertThat(response.token()).isEqualTo("token");
        assertThat(response.message()).isEqualTo("success");
    }

    @Test
    void userRequestDtoTest() {
        UserRequestDto dto = new UserRequestDto("Name", "login", "pass");
        assertThat(dto.name()).isEqualTo("Name");
        assertThat(dto.login()).isEqualTo("login");
        assertThat(dto.password()).isEqualTo("pass");
    }

    @Test
    void userResponseDtoTest() {
        UUID id = UUID.randomUUID();
        UserResponseDto dto = new UserResponseDto(id, "Name", "login");
        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.name()).isEqualTo("Name");
        assertThat(dto.login()).isEqualTo("login");
    }

    @Test
    void userMapperTest() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "Name", "login", "pass");
        UserResponseDto responseDto = UserMapper.toResponseDto(user);
        assertThat(responseDto.id()).isEqualTo(id);
        assertThat(responseDto.name()).isEqualTo("Name");
        assertThat(responseDto.login()).isEqualTo("login");

        MongoUser mongoUser = new MongoUser(id.toString(), "Name", "login", "pass");
        UserResponseDto mongoResponseDto = UserMapper.toResponseDto(mongoUser);
        assertThat(mongoResponseDto.id().toString()).isEqualTo(id.toString());
        assertThat(mongoResponseDto.name()).isEqualTo("Name");
        assertThat(mongoResponseDto.login()).isEqualTo("login");

        UserRequestDto requestDto = new UserRequestDto("New Name", "newlogin", "newpass");
        User mappedUser = UserMapper.toEntity(requestDto);
        assertThat(mappedUser.getName()).isEqualTo("New Name");
        assertThat(mappedUser.getLogin()).isEqualTo("newlogin");
        assertThat(mappedUser.getPassword()).isEqualTo("newpass");

        MongoUser mappedMongoUser = UserMapper.toMongoEntity(requestDto);
        assertThat(mappedMongoUser.getName()).isEqualTo("New Name");
        assertThat(mappedMongoUser.getLogin()).isEqualTo("newlogin");
        assertThat(mappedMongoUser.getPassword()).isEqualTo("newpass");
    }
}
