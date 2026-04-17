package com.cis.api.config;

import com.cis.api.repository.MongoPersistencePort;
import com.cis.api.repository.MySqlPersistencePort;
import com.cis.api.repository.UserPersistencePort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RepositoryConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "db.type", havingValue = "mysql", matchIfMissing = true)
    public UserPersistencePort mySqlUserPersistencePort(MySqlPersistencePort mySqlAdapter) {
        return mySqlAdapter;
    }

    @Bean
    @ConditionalOnProperty(name = "db.type", havingValue = "mongo")
    public UserPersistencePort mongoUserPersistencePort(MongoPersistencePort mongoAdapter) {
        return mongoAdapter;
    }
}
