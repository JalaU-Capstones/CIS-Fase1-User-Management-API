package com.cis.api.repository;

import com.cis.api.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindAllUsers() {
        // given
        User user1 = new User(UUID.randomUUID(), "John Doe", "jdoe", "secret1");
        User user2 = new User(UUID.randomUUID(), "Jane Smith", "jsmith", "secret2");
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        // when
        List<User> users = userRepository.findAll();

        // then
        assertThat(users).hasSize(2)
                .extracting(User::getLogin)
                .containsExactlyInAnyOrder("jdoe", "jsmith");
    }

    @Test
    void existsByLogin_WhenLoginExists_ShouldReturnTrue() {
        // given
        User user = new User(UUID.randomUUID(), "Test User", "testlogin", "password");
        entityManager.persist(user);
        entityManager.flush();

        // when
        boolean exists = userRepository.existsByLogin("testlogin");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByLogin_WhenLoginDoesNotExist_ShouldReturnFalse() {
        // when
        boolean exists = userRepository.existsByLogin("nonexistent");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void saveUser_ShouldGenerateAndReturnId() {
        // given
        User user = new User();
        user.setName("New User");
        user.setLogin("newuser");
        user.setPassword("password123");

        // when
        User savedUser = userRepository.save(user);

        // then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getId()).isInstanceOf(UUID.class);
        assertThat(savedUser.getName()).isEqualTo("New User");
        assertThat(savedUser.getLogin()).isEqualTo("newuser");
    }
}