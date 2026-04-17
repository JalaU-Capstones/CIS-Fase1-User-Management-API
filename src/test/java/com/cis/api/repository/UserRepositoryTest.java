package com.cis.api.repository;

import com.cis.api.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldFindAllUsers() {
        User user1 = new User(UUID.randomUUID(), "John Doe", "jdoe", "secret1");
        User user2 = new User(UUID.randomUUID(), "Jane Smith", "jsmith", "secret2");
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        List<User> users = userRepository.findAll();

        assertThat(users).hasSize(2)
                .extracting(User::getLogin)
                .containsExactlyInAnyOrder("jdoe", "jsmith");
    }

    @Test
    void shouldFindUserById() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "Paula", "pmartin", "pass");
        entityManager.persist(user);
        entityManager.flush();

        var result = userRepository.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getLogin()).isEqualTo("pmartin");
        assertThat(result.get().getName()).isEqualTo("Paula");
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        UUID id = UUID.randomUUID();
        var result = userRepository.findById(id);
        assertThat(result).isEmpty();
    }

    @Test
    void existsByLogin_WhenLoginExists_ShouldReturnTrue() {
        User user = new User(UUID.randomUUID(), "Test User", "testlogin", "password");
        entityManager.persist(user);
        entityManager.flush();

        boolean exists = userRepository.existsByLogin("testlogin");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByLogin_WhenLoginDoesNotExist_ShouldReturnFalse() {
        boolean exists = userRepository.existsByLogin("nonexistent");
        assertThat(exists).isFalse();
    }

    @Test
    void saveUser_ShouldGenerateAndReturnId() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("New User");
        user.setLogin("newuser");
        user.setPassword("password123");

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("New User");
        assertThat(savedUser.getLogin()).isEqualTo("newuser");
    }

    @Test
    void existsByLoginAndIdNot_WhenLoginExistsForDifferentUser_ShouldReturnTrue() {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        entityManager.persist(new User(userId1, "User One", "loginshared", "pass1"));
        entityManager.persist(new User(userId2, "User Two", "otherone", "pass2"));
        entityManager.flush();

        boolean exists = userRepository.existsByLoginAndIdNot("loginshared", userId2);

        assertThat(exists).isTrue();
    }

    @Test
    void existsByLoginAndIdNot_WhenLoginBelongsToSameUser_ShouldReturnFalse() {
        UUID userId = UUID.randomUUID();
        entityManager.persist(new User(userId, "User One", "mylogin", "pass1"));
        entityManager.flush();

        boolean exists = userRepository.existsByLoginAndIdNot("mylogin", userId);

        assertThat(exists).isFalse();
    }

    @Test
    void deleteUser_ShouldRemoveUserFromDatabase() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "To Delete", "todelete", "pass123");
        entityManager.persist(user);
        entityManager.flush();

        userRepository.delete(user);
        entityManager.flush();

        var result = userRepository.findById(id);
        assertThat(result).isEmpty();
    }

    @Test
    @Transactional
    @Sql("/init.sql")
    void shouldDeleteTopicsIdeasAndVotesInCascade() {
        // 1. Create User
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "Cascade Owner", "cascade", "pass");
        entityManager.persist(user);
        entityManager.flush();

        // 2. Create Topic owned by user
        UUID topicId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO topics (id, title, description, status, owner_id) VALUES (?, ?, ?, ?, ?)",
                topicId.toString(), "Title", "Desc", "OPEN", userId.toString());

        // 3. Create Idea for Topic, owned by user
        UUID ideaId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO ideas (id, content, topic_id, owner_id) VALUES (?, ?, ?, ?)",
                ideaId.toString(), "Idea content", topicId.toString(), userId.toString());

        // 4. Create Vote for Idea, by user
        UUID voteId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO votes (id, idea_id, user_id) VALUES (?, ?, ?)",
                voteId.toString(), ideaId.toString(), userId.toString());

        // 5. Verify they exist
        assertThat(count("topics", userId)).isEqualTo(1);
        assertThat(count("ideas", userId)).isEqualTo(1);
        assertThat(countVotes(userId)).isEqualTo(1);

        // 6. Execute Deletions (mimicking UserService/Adapter order)
        userRepository.deleteVotesByUserId(userId.toString());
        userRepository.deleteVotesByIdeasOwnedByUserId(userId.toString());
        userRepository.deleteVotesByIdeasLinkedToTopicsOwnedByUserId(userId.toString());
        
        userRepository.deleteIdeasByUserId(userId.toString());
        userRepository.deleteIdeasLinkedToTopicsOwnedByUserId(userId.toString());
        
        userRepository.deleteTopicsByUserId(userId.toString());
        
        userRepository.deleteUserByIdNative(userId.toString());
        entityManager.flush();

        // 7. Verify all gone
        assertThat(userRepository.findById(userId)).isEmpty();
        assertThat(count("topics", userId)).isZero();
        assertThat(count("ideas", userId)).isZero();
        assertThat(countVotes(userId)).isZero();
    }

    private Integer count(String table, UUID userId) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE owner_id = ?", Integer.class, userId.toString());
    }

    private Integer countVotes(UUID userId) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM votes WHERE user_id = ?", Integer.class, userId.toString());
    }
}
