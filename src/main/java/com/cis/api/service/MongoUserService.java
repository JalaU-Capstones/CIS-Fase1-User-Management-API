package com.cis.api.service;

import com.cis.api.dto.UserMapper;
import com.cis.api.dto.UserRequestDto;
import com.cis.api.dto.UserResponseDto;
import com.cis.api.exception.ResourceNotFoundException;
import com.cis.api.fallback.DatabaseFallbackService;
import com.cis.api.model.User;
import com.cis.api.repository.UserPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.UUID;
import java.util.Set;
import java.util.Collection;
import java.util.ArrayList;

@Profile("!migrate")
@Service
@RequiredArgsConstructor
public class MongoUserService {

    private final UserPersistencePort userPersistencePort;
    private final PasswordEncoder passwordEncoder;
    private final MongoTemplate mongoTemplate;
    private final ObjectProvider<DatabaseFallbackService> databaseFallbackServiceProvider;

    public List<UserResponseDto> getAllUsers() {
        return userPersistencePort.findAll().stream()
                .map(UserMapper::toResponseDto)
                .toList();
    }

    public UserResponseDto getUserById(String id) {
        return userPersistencePort.findById(UUID.fromString(id))
                .map(UserMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public UserResponseDto createUser(UserRequestDto request) {
        validateLoginUniqueness(request.login());

        User user = UserMapper.toEntity(request);
        user.setId(UUID.randomUUID());
        user.setPassword(passwordEncoder.encode(request.password()));

        return UserMapper.toResponseDto(userPersistencePort.save(user));
    }

    public UserResponseDto updateUser(String id, UserRequestDto request) {
        UUID uuid = UUID.fromString(id);
        User user = findUserById(uuid);

        checkOwnership(user);

        if (!user.getLogin().equals(request.login())) {
            validateLoginUniqueness(request.login(), uuid);
        }

        user.setName(request.name());
        user.setLogin(request.login());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        return UserMapper.toResponseDto(userPersistencePort.save(user));
    }

    @Transactional
    public void deleteUser(String id) {
        UUID uuid = UUID.fromString(id);
        User user = findUserById(uuid);

        checkOwnership(user);

        DatabaseFallbackService fallbackService = databaseFallbackServiceProvider.getIfAvailable();
        if (fallbackService != null) {
            // If fallback is routing v2 requests to MySQL, use the MySQL deletion strategy.
            if (DatabaseFallbackService.DB_MYSQL.equals(fallbackService.getActiveDatabase("v2"))) {
                userPersistencePort.deleteUserAndRelatedData(uuid);
                return;
            }
        }

        String userId = uuid.toString();

        deleteVotesByUser(userId);
        deleteIdeasOwnedByUser(userId);
        deleteTopicsOwnedByUser(userId);
        mongoTemplate.remove(Query.query(Criteria.where("_id").is(userId)), "users");
    }

    private void deleteVotesByUser(String userId) {
        mongoTemplate.remove(Query.query(anyOf(userId, "user_id", "userId", "UserId")), "votes");
    }

    private void deleteIdeasOwnedByUser(String userId) {
        List<String> ideaIds = mongoTemplate.findDistinct(
                Query.query(anyOf(userId, "owner_id", "ownerId", "OwnerId")),
                "_id",
                "ideas",
                String.class
        );

        deleteVotesByIdeaIds(ideaIds);
        mongoTemplate.remove(Query.query(anyOf(userId, "owner_id", "ownerId", "OwnerId")), "ideas");
    }

    private void deleteTopicsOwnedByUser(String userId) {
        mongoTemplate.remove(Query.query(anyOf(userId, "owner_id", "ownerId", "OwnerId")), "topics");
    }

    private void deleteVotesByIdeaIds(List<String> ideaIds) {
        Set<String> uniqueIdeaIds = new LinkedHashSet<>(ideaIds);
        if (uniqueIdeaIds.isEmpty()) {
            return;
        }

        mongoTemplate.remove(Query.query(anyOf(uniqueIdeaIds, "idea_id", "ideaId", "IdeaId")), "votes");
    }

    private Criteria anyOf(Object value, String... fieldNames) {
        List<Criteria> alternatives = new ArrayList<>();

        if (value instanceof Collection<?> values) {
            for (String fieldName : fieldNames) {
                alternatives.add(Criteria.where(fieldName).in(values));
            }
        } else {
            for (String fieldName : fieldNames) {
                alternatives.add(Criteria.where(fieldName).is(value));
            }
        }

        return new Criteria().orOperator(alternatives.toArray(Criteria[]::new));
    }

    private User findUserById(UUID id) {
        return userPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private void checkOwnership(User user) {
        String currentUserLogin = getCurrentUserLogin();
        if (!user.getLogin().equals(currentUserLogin)) {
            throw new AccessDeniedException("You can only modify your own user record.");
        }
    }

    private String getCurrentUserLogin() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }

    private void validateLoginUniqueness(String login) {
        if (userPersistencePort.existsByLogin(login)) {
            throw new IllegalArgumentException("Login already exists: " + login);
        }
    }

    private void validateLoginUniqueness(String login, UUID userId) {
        if (userPersistencePort.existsByLoginAndIdNot(login, userId)) {
            throw new IllegalArgumentException("Login already exists: " + login);
        }
    }
}
