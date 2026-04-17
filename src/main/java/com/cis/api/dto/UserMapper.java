package com.cis.api.dto;

import com.cis.api.model.MongoUser;
import com.cis.api.model.User;
import java.util.UUID;

public class UserMapper {

    public static UserResponseDto toResponseDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getLogin()
        );
    }

    public static UserResponseDto toResponseDto(MongoUser user) {
        return new UserResponseDto(
                user.getId() != null ? UUID.fromString(user.getId()) : null,
                user.getName(),
                user.getLogin()
        );
    }

    public static User toEntity(UserRequestDto dto) {
        User user = new User();
        user.setName(dto.name());
        user.setLogin(dto.login());
        user.setPassword(dto.password());
        return user;
    }

    public static MongoUser toMongoEntity(UserRequestDto dto) {
        MongoUser user = new MongoUser();
        user.setName(dto.name());
        user.setLogin(dto.login());
        user.setPassword(dto.password());
        return user;
    }
}
