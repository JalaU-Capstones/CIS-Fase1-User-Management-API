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
}
