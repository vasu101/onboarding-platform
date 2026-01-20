package com.onboarding.platform.security.service;


import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Service for password hashing and verification.
 * Uses BCrypt for secure password storage with built-in salting and adaptive cost.
 */
@Singleton
public class PasswordEncoderService {

    private final PasswordEncoder delegate = new BCryptPasswordEncoder();

    public PasswordEncoderService() {
    }

    /**
     * Encode password using BCrypt
     */
    public String encode(@NotBlank @NonNull String plainPassword) {
        return delegate.encode(plainPassword);
    }

    /**
     * Verify plain password against encoded password
     */
    public boolean matches(@NotBlank @NonNull String plainPassword,
                           @NotBlank @NonNull String encodedPassword) {
        return delegate.matches(plainPassword, encodedPassword);
    }
}