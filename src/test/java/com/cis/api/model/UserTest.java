package com.cis.api.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void shouldCreateUserWithAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "John Doe", "jdoe", "password");

        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getName()).isEqualTo("John Doe");
        assertThat(user.getLogin()).isEqualTo("jdoe");
        assertThat(user.getPassword()).isEqualTo("password");
    }

    @Test
    void shouldCreateUserWithNoArgsConstructorAndSetters() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setName("Jane Smith");
        user.setLogin("jsmith");
        user.setPassword("newpassword");

        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getName()).isEqualTo("Jane Smith");
        assertThat(user.getLogin()).isEqualTo("jsmith");
        assertThat(user.getPassword()).isEqualTo("newpassword");
    }

    @Test
    void testEqualsAndHashCode() {
        UUID id = UUID.randomUUID();
        User user1 = new User(id, "User", "login", "pass");
        User user2 = new User(id, "User", "login", "pass");
        User user3 = new User(UUID.randomUUID(), "User", "login", "pass");

        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
        assertThat(user1).isNotEqualTo(user3);
    }

    @Test
    void testToString() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "User", "login", "pass");
        assertThat(user.toString()).contains(id.toString());
    }
}
