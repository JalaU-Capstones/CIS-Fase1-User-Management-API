package com.cis.api.repository;

import com.cis.api.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserPersistencePort {
    List<User> findAll();
    Optional<User> findById(UUID id);
    Optional<User> findByLogin(String login);
    User save(User user);
    void deleteById(UUID id);
    boolean existsByLogin(String login);
    boolean existsByLoginAndIdNot(String login, UUID id);
    void deleteUserAndRelatedData(UUID id);
}
