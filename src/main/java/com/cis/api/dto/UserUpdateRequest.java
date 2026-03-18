package com.cis.api.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing user.
 * All fields are optional to allow partial updates (though typically PUT replaces the resource).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {
    @Size(max = 200)
    private String name;

    @Size(max = 20)
    private String login;

    @Size(min = 6, max = 100)
    private String password;
}
