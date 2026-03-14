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
    /**
     * Verifies that findById returns the correct user when the user exists.
     * Uses TestEntityManager to insert a user directly into H2 in-memory database
     * without going through the service layer.
     */
    @Test
    void shouldFindUserById() {
        // given
        UUID id = UUID.randomUUID();
        User user = new User(id, "Paula", "pmartin", "pass");
        entityManager.persist(user);
        entityManager.flush();

        // when
        var result = userRepository.findById(id);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getLogin()).isEqualTo("pmartin");
        assertThat(result.get().getName()).isEqualTo("Paula");
    }

    /**
     * Verifies that findById returns empty when the user does not exist.
     * Optional.empty() is expected just an empty response, not exception.
     */
    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        // given
        UUID id = UUID.randomUUID();

        // when
        var result = userRepository.findById(id);

        // then
        assertThat(result).isEmpty();
    }
}
