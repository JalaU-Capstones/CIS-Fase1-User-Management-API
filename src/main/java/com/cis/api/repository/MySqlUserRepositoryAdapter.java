package com.cis.api.repository;

import com.cis.api.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MySqlUserRepositoryAdapter implements MySqlPersistencePort {

    private final UserRepository userRepository;

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteById(UUID id) {
        userRepository.deleteById(id);
    }

    @Override
    public boolean existsByLogin(String login) {
        return userRepository.existsByLogin(login);
    }

    @Override
    public boolean existsByLoginAndIdNot(String login, UUID id) {
        return userRepository.existsByLoginAndIdNot(login, id);
    }

    @Override
    public void deleteUserAndRelatedData(UUID id) {
        String uuidStr = id.toString();
        userRepository.deleteVotesByIdeasLinkedToTopicsOwnedByUserId(uuidStr);
        userRepository.deleteVotesByIdeasOwnedByUserId(uuidStr);
        userRepository.deleteVotesByUserId(uuidStr);
        userRepository.flush();

        userRepository.deleteIdeasLinkedToTopicsOwnedByUserId(uuidStr);
        userRepository.deleteIdeasByUserId(uuidStr);
        userRepository.flush();

        userRepository.deleteTopicsByUserId(uuidStr);
        userRepository.flush();

        userRepository.deleteUserByIdNative(uuidStr);
        userRepository.flush();
    }
}
