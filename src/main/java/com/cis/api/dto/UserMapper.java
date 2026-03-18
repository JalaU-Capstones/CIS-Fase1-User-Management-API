package com.cis.api.dto;

import com.cis.api.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDto toDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getLogin()
        );
    }

    public void updateUserFromDto(UserRequestDto dto, User user) {
        user.setName(dto.name());
        user.setLogin(dto.login());
    }
}
