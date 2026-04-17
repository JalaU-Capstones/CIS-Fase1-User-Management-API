package com.cis.api.dto;

import com.cis.api.model.User;
import com.cis.api.model.MongoUser;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    void toResponseDto_ShouldMapUserFieldsCorrectly() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "John Doe", "jdoe", "pass");

        UserResponseDto dto = UserMapper.toResponseDto(user);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.name()).isEqualTo("John Doe");
        assertThat(dto.login()).isEqualTo("jdoe");
    }

    @Test
    void toResponseDto_ShouldMapMongoUserFieldsCorrectly() {
        String id = UUID.randomUUID().toString();
        MongoUser user = new MongoUser(id, "John Doe", "jdoe", "pass");

        UserResponseDto dto = UserMapper.toResponseDto(user);

        assertThat(dto.id().toString()).isEqualTo(id);
        assertThat(dto.name()).isEqualTo("John Doe");
        assertThat(dto.login()).isEqualTo("jdoe");
    }

    @Test
    void toEntity_ShouldMapDtoToUser() {
        UserRequestDto dto = new UserRequestDto("Jane Smith", "jsmith", "newpass");

        User user = UserMapper.toEntity(dto);

        assertThat(user.getName()).isEqualTo("Jane Smith");
        assertThat(user.getLogin()).isEqualTo("jsmith");
        assertThat(user.getPassword()).isEqualTo("newpass");
    }

    @Test
    void toMongoEntity_ShouldMapDtoToMongoUser() {
        UserRequestDto dto = new UserRequestDto("Jane Smith", "jsmith", "newpass");

        MongoUser user = UserMapper.toMongoEntity(dto);

        assertThat(user.getName()).isEqualTo("Jane Smith");
        assertThat(user.getLogin()).isEqualTo("jsmith");
        assertThat(user.getPassword()).isEqualTo("newpass");
    }
}
