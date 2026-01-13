package com.onboarding.platform.security.repository;

import com.onboarding.platform.security.model.User;
import com.onboarding.platform.security.model.UserRole;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for user entities
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find all users by role
     */
    List<User> findByRole(UserRole role);

    /**
     * Find all active users
     */
    List<User> findByActiveTrue();

    /**
     * Find active user by username
     */
    Optional<User> findByUsernameAndActiveTrue(String username);
}
