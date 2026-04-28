package com.cis.api.fallback;

import com.cis.api.model.User;
import com.cis.api.repository.MongoPersistencePort;
import com.cis.api.repository.MySqlPersistencePort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class RoutingUserPersistencePortTest {

    @AfterEach
    void cleanup() {
        RequestVersionContext.clear();
    }

    @Test
    void delegatesToMySqlWhenActiveDatabaseIsMySql() {
        MySqlPersistencePort mysql = mock(MySqlPersistencePort.class);
        MongoPersistencePort mongo = mock(MongoPersistencePort.class);
        DatabaseFallbackService fallbackService = mock(DatabaseFallbackService.class);

        RequestVersionContext.setOriginalVersion("v1");
        given(fallbackService.getActiveDatabase("v1")).willReturn(DatabaseFallbackService.DB_MYSQL);

        User u = new User(UUID.randomUUID(), "A", "a", "p");
        given(mysql.findAll()).willReturn(List.of(u));

        RoutingUserPersistencePort router = new RoutingUserPersistencePort(mysql, mongo, fallbackService);

        assertThat(router.findAll()).hasSize(1);
        then(mysql).should().findAll();
    }

    @Test
    void delegatesToMongoWhenActiveDatabaseIsMongo() {
        MySqlPersistencePort mysql = mock(MySqlPersistencePort.class);
        MongoPersistencePort mongo = mock(MongoPersistencePort.class);
        DatabaseFallbackService fallbackService = mock(DatabaseFallbackService.class);

        RequestVersionContext.setOriginalVersion("v2");
        given(fallbackService.getActiveDatabase("v2")).willReturn(DatabaseFallbackService.DB_MONGO);

        UUID id = UUID.randomUUID();
        User u = new User(id, "B", "b", "p");
        given(mongo.findById(id)).willReturn(Optional.of(u));

        RoutingUserPersistencePort router = new RoutingUserPersistencePort(mysql, mongo, fallbackService);

        assertThat(router.findById(id)).contains(u);
        then(mongo).should().findById(id);
    }
}
