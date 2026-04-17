package com.cis.api.model;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class ModelTests {

    @Test
    void userModelTest() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "Name", "login", "pass");
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getName()).isEqualTo("Name");
        assertThat(user.getLogin()).isEqualTo("login");
        assertThat(user.getPassword()).isEqualTo("pass");

        User emptyUser = new User();
        emptyUser.setId(id);
        assertThat(emptyUser.getId()).isEqualTo(id);
    }

    @Test
    void mongoUserModelTest() {
        UUID id = UUID.randomUUID();
        MongoUser user = new MongoUser(id, "Name", "login", "pass");
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getName()).isEqualTo("Name");
        assertThat(user.getLogin()).isEqualTo("login");
        assertThat(user.getPassword()).isEqualTo("pass");

        MongoUser emptyUser = new MongoUser();
        emptyUser.setId(id);
        assertThat(emptyUser.getId()).isEqualTo(id);
    }
}
