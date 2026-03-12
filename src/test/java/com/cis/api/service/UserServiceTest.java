package com.cis.api.service;

import com.cis.api.dto.UserResponseDto;
import com.cis.api.model.User;
import com.cis.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnListOfUsersAsDtos() {
        // given
        User user = new User(UUID.randomUUID(), "Test User", "test", "pass");
        given(userRepository.findAll()).willReturn(List.of(user));

        // when
        List<UserResponseDto> result = userService.getAllUsers();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).login()).isEqualTo("test");
        assertThat(result.get(0).id()).isEqualTo(user.getId());
        then(userRepository).should().findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoUsers() {
        // given
        given(userRepository.findAll()).willReturn(Collections.emptyList());

        // when
        List<UserResponseDto> result = userService.getAllUsers();

        // then
        assertThat(result).isEmpty();
    }
}
