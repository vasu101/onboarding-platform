package com.onboarding.platform.api.dto;

import com.onboarding.platform.security.model.UserRole;
import io.micronaut.serde.annotation.Serdeable;

import java.util.UUID;

@Serdeable
public class AuthResponse {

    private String token;
    private UUID userId;
    private String username;
    private String email;
    private String fullName;
    private UserRole role;
    private long expiresIn;

    public AuthResponse() {
    }

    public AuthResponse(String token, UUID userId, String username, String email, String fullName, UserRole role, long expiresIn) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.expiresIn = expiresIn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
