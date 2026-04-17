package com.cis.api.repository;

import com.cis.api.model.MongoUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
import java.util.UUID;

public interface MongoUserSpringRepository extends MongoRepository<MongoUser, UUID> {
    Optional<MongoUser> findByLogin(String login);
    boolean existsByLogin(String login);
    boolean existsByLoginAndIdNot(String login, UUID id);
}
