package com.cis.api.repository;

import com.cis.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for User entity.
 * Provides standard CRUD operations via Spring Data JPA.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // Standard JPA methods are sufficient for US 1.1.1
}
