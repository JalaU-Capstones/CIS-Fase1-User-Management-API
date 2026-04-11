package com.cis.api.dto;

import com.cis.api.model.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    void toDto_ShouldMapFieldsCorrectly() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "John Doe", "jdoe", "pass");

        UserResponseDto dto = userMapper.toDto(user);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.name()).isEqualTo("John Doe");
        assertThat(dto.login()).isEqualTo("jdoe");
    }

    @Test
    void updateUserFromDto_ShouldUpdateFields() {
        User user = new User();
        UserRequestDto dto = new UserRequestDto("Jane Smith", "jsmith", "newpass");

        userMapper.updateUserFromDto(dto, user);

        assertThat(user.getName()).isEqualTo("Jane Smith");
        assertThat(user.getLogin()).isEqualTo("jsmith");
        // Password should NOT be updated by the mapper as it's handled in the service
    }
}
