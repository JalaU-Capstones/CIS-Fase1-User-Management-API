package com.cis.api.config;

import com.cis.api.repository.MongoPersistencePort;
import com.cis.api.repository.MySqlPersistencePort;
import com.cis.api.repository.UserPersistencePort;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RepositoryConfigTest {

    @Test
    void shouldReturnMySqlPort() {
        RepositoryConfig config = new RepositoryConfig();
        MySqlPersistencePort mysql = mock(MySqlPersistencePort.class);
        UserPersistencePort port = config.mySqlUserPersistencePort(mysql);
        assertThat(port).isEqualTo(mysql);
    }

    @Test
    void shouldReturnMongoPort() {
        RepositoryConfig config = new RepositoryConfig();
        MongoPersistencePort mongo = mock(MongoPersistencePort.class);
        UserPersistencePort port = config.mongoUserPersistencePort(mongo);
        assertThat(port).isEqualTo(mongo);
    }
}
