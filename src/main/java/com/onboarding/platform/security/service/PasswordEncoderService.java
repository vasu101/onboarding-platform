package com.onboarding.platform.security.service;

import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for password hashing and verification.
 * Uses SHA-256 with salt for secure password storage.
 */
@Singleton
public class PasswordEncoderService {

    private static final Logger LOG = LoggerFactory.getLogger(PasswordEncoderService.class);
    private static final String ALGORITHM = "SHA_256";
    private static final int SALT_LENGTH = 16;
    private final SecureRandom secureRandom;

    public PasswordEncoderService(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    /**
     * Encode password with salt
     */
    public String encode(String plainPassword) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            secureRandom.nextBytes(salt);

            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(salt);
            byte[] hashedPassword = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined,0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Failed to encode password", e);
            throw new RuntimeException("Password encoding failed", e);
        }
    }

    /**
     * Verify plain password against encoded password
     */
    public boolean matches(String plainPassword, String encodedPassword) {
        try {
            byte[] combined = Base64.getDecoder().decode(encodedPassword);

            byte[] salt = new byte[SALT_LENGTH];
            byte[] storedHash = new byte[combined.length - SALT_LENGTH];
            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);
            System.arraycopy(combined, SALT_LENGTH, storedHash, 0, storedHash.length);

            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(salt);
            byte[] inputHash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));

            return MessageDigest.isEqual(storedHash, inputHash);

        } catch (Exception e) {
            LOG.error("Failed to verify password", e);
            return false;
        }
    }
}
