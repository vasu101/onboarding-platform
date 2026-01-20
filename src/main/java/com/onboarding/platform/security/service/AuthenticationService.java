package com.onboarding.platform.security.service;


import com.onboarding.platform.security.model.User;
import com.onboarding.platform.security.model.UserRole;
import com.onboarding.platform.security.repository.UserRepository;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for user authentication and registration
 */
@Singleton
public class AuthenticationService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final PasswordEncoderService passwordEncoder;

    public AuthenticationService(UserRepository userRepository, PasswordEncoderService passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user
     */
    @Transactional
    public User register(String username, String email, String password, String fullName, UserRole role) {
        LOG.info("Registering new user: {}", username);

        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole(role);
        user.setActive(true);
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);
        LOG.info("User registered successfully: {}", username);

        return savedUser;
    }

    /**
     * Authenticate user with username and password
     */
    public Optional<User> authenticate(String username, String password) {
        LOG.info("Authenticating user: {}", username);

        Optional<User> userOpt = userRepository.findByUsernameAndActiveTrue(username);

        if (userOpt.isEmpty()) {
            LOG.warn("User not found or inactive: {}", username);
            return Optional.empty();
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            LOG.warn("Invalid password for user: {}", username);
            return Optional.empty();
        }

        // Update last login
        updateLastLogin(user);

        LOG.info("User authenticated successfully: {}", username);
        return Optional.of(user);
    }

    @Transactional
    public void updateLastLogin(User user) {
        user.setLastLoginAt(Instant.now());
        userRepository.update(user);
    }

    @Transactional
    public void changePassword(User user, String oldPassword, String newPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.update(user);
        LOG.info("Password changed for user: {}", user.getUsername());
    }

    public Optional<User> findByUsername(@NotBlank @NonNull String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Deactivate user
     */
    @Transactional
    public void deactivateUser(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setActive(false);
            userRepository.update(user);
            LOG.info("User deactivated: {}", user.getUsername());
        });
    }
}
