package com.cis.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MongoTransactionConfigTest {

    @Test
    void shouldCreateMongoTransactionManager() {
        MongoTransactionConfig config = new MongoTransactionConfig();
        MongoDatabaseFactory factory = mock(MongoDatabaseFactory.class);
        
        MongoTransactionManager manager = config.mongoTransactionManager(factory);
        
        assertThat(manager).isNotNull();
    }
}
