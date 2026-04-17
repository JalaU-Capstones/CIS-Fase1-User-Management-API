package com.cis.api.repository;

import com.cis.api.model.MongoUser;
import com.cis.api.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MongoUserRepositoryAdapter implements MongoPersistencePort {

    private final MongoUserSpringRepository mongoRepository;

    @Override
    public List<User> findAll() {
        return mongoRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> findById(UUID id) {
        return mongoRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByLogin(String login) {
        return mongoRepository.findByLogin(login).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        MongoUser mongoUser = toMongo(user);
        MongoUser saved = mongoRepository.save(mongoUser);
        return toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        mongoRepository.deleteById(id);
    }

    @Override
    public boolean existsByLogin(String login) {
        return mongoRepository.existsByLogin(login);
    }

    @Override
    public boolean existsByLoginAndIdNot(String login, UUID id) {
        return mongoRepository.existsByLoginAndIdNot(login, id);
    }

    @Override
    public void deleteUserAndRelatedData(UUID id) {
        mongoRepository.deleteById(id);
    }

    private User toDomain(MongoUser mongoUser) {
        User user = new User();
        user.setId(mongoUser.getId());
        user.setName(mongoUser.getName());
        user.setLogin(mongoUser.getLogin());
        user.setPassword(mongoUser.getPassword());
        return user;
    }

    private MongoUser toMongo(User user) {
        return new MongoUser(user.getId(), user.getName(), user.getLogin(), user.getPassword());
    }
}
