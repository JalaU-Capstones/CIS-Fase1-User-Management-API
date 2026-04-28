package com.cis.api.fallback;

import com.cis.api.repository.MongoPersistencePort;
import com.cis.api.repository.MySqlPersistencePort;
import com.cis.api.repository.UserPersistencePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Routes calls to the correct persistence adapter (MySQL/MongoDB) based on the current fallback decision.
 */
@Slf4j
@Primary
@Component
@ConditionalOnProperty(prefix = "app.fallback", name = "enabled", havingValue = "true")
public class RoutingUserPersistencePort implements UserPersistencePort {

    private final MySqlPersistencePort mySqlPersistencePort;
    private final MongoPersistencePort mongoPersistencePort;
    private final DatabaseFallbackService databaseFallbackService;

    public RoutingUserPersistencePort(
            MySqlPersistencePort mySqlPersistencePort,
            MongoPersistencePort mongoPersistencePort,
            DatabaseFallbackService databaseFallbackService
    ) {
        this.mySqlPersistencePort = mySqlPersistencePort;
        this.mongoPersistencePort = mongoPersistencePort;
        this.databaseFallbackService = databaseFallbackService;
    }

    @Override
    public List<com.cis.api.model.User> findAll() {
        return delegate().findAll();
    }

    @Override
    public Optional<com.cis.api.model.User> findById(UUID id) {
        return delegate().findById(id);
    }

    @Override
    public Optional<com.cis.api.model.User> findByLogin(String login) {
        return delegate().findByLogin(login);
    }

    @Override
    public com.cis.api.model.User save(com.cis.api.model.User user) {
        return delegate().save(user);
    }

    @Override
    public void deleteById(UUID id) {
        delegate().deleteById(id);
    }

    @Override
    public boolean existsByLogin(String login) {
        return delegate().existsByLogin(login);
    }

    @Override
    public boolean existsByLoginAndIdNot(String login, UUID id) {
        return delegate().existsByLoginAndIdNot(login, id);
    }

    @Override
    public void deleteUserAndRelatedData(UUID id) {
        delegate().deleteUserAndRelatedData(id);
    }

    private UserPersistencePort delegate() {
        String version = RequestVersionContext.getOriginalVersion();
        String active = databaseFallbackService.getActiveDatabase(version);
        return switch (active) {
            case DatabaseFallbackService.DB_MONGO -> mongoPersistencePort;
            case DatabaseFallbackService.DB_MYSQL -> mySqlPersistencePort;
            default -> {
                log.warn("Unknown active database '{}', defaulting to MySQL", active);
                yield mySqlPersistencePort;
            }
        };
    }
}
