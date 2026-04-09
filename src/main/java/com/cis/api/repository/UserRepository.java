package com.cis.api.repository;

import com.cis.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity.
 * Provides standard CRUD operations via Spring Data JPA.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * Finds a user by their login username.
     * @param login the username
     * @return Optional containing the User if found
     */
    Optional<User> findByLogin(String login);

    // Method for US 1.2.1
    boolean existsByLogin(String login);
    boolean existsByLoginAndIdNot(String login, UUID id);

    @Modifying
    @Query(value = "DELETE FROM topics WHERE user_id = :userId", nativeQuery = true)
    void deleteTopicsByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query(value = "DELETE FROM ideas WHERE user_id = :userId", nativeQuery = true)
    void deleteIdeasByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query(value = "DELETE FROM votes WHERE user_id = :userId", nativeQuery = true)
    void deleteVotesByUserId(@Param("userId") UUID userId);
}
