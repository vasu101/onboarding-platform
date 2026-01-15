package com.onboarding.platform.api.controller;

import com.onboarding.platform.api.dto.auth.AuthResponse;
import com.onboarding.platform.api.dto.auth.LoginRequest;
import com.onboarding.platform.api.dto.auth.RegisterRequest;
import com.onboarding.platform.security.model.UserRole;
import com.onboarding.platform.security.repository.UserRepository;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AuthController
 */
@MicronautTest
public class AuthControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    UserRepository userRepository;

    @AfterEach
    void cleanup() {
        userRepository.findByEmail("test@integration.com")
                .ifPresent(userRepository::delete);
    }

    @Test
    void testRegister() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testUser");
        request.setEmail("test@integration.com");
        request.setPassword("password123");
        request.setFullName("Test User");
        request.setRole(UserRole.CUSTOMER);

        HttpResponse<AuthResponse> response = client.toBlocking().exchange(
                HttpRequest.POST("/api/auth/register", request),
                AuthResponse.class
        );

        assertEquals(HttpStatus.CREATED, response.status());
        assertNotNull(response.body());
        assertNotNull(response.body().getToken());
        assertEquals("testUser", response.body().getUsername());
        assertEquals(UserRole.CUSTOMER, response.body().getRole());
    }

    @Test
    void testRegister_DuplicateUsername() {
        // Given - First registration
        RegisterRequest request1 = new RegisterRequest();
        request1.setUsername("duplicate");
        request1.setEmail("first@integration.com");
        request1.setPassword("password123");
        request1.setFullName("First User");
        request1.setRole(UserRole.CUSTOMER);

        client.toBlocking().exchange(
                HttpRequest.POST("/api/auth/register", request1),
                AuthResponse.class
        );

        // When - Try duplicate username
        RegisterRequest request2 = new RegisterRequest();
        request2.setUsername("duplicate");
        request2.setEmail("second@integration.com");
        request2.setPassword("password123");
        request2.setFullName("Second User");
        request2.setRole(UserRole.CUSTOMER);

        // Then
        try {
            client.toBlocking().exchange(
                    HttpRequest.POST("/api/auth/register", request2),
                    AuthResponse.class
            );
            fail("Should have thrown exception");
        } catch (Exception e) {
            // Expected
        } finally {
            // Cleanup
            userRepository.findByEmail("first@integration.com")
                    .ifPresent(userRepository::delete);
        }
    }

    @Test
    void testLogin() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("loginTest");
        registerRequest.setEmail("login@integration.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Login Test");
        registerRequest.setRole(UserRole.CUSTOMER);

        client.toBlocking().exchange(
                HttpRequest.POST("/api/auth/register", registerRequest),
                AuthResponse.class
        );

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("loginTest");
        loginRequest.setPassword("password123");

        HttpResponse<AuthResponse> response = client.toBlocking().exchange(
                HttpRequest.POST("/api/auth/login", loginRequest),
                AuthResponse.class
        );

        assertEquals(HttpStatus.OK, response.status());
        assertNotNull(response.body());
        assertNotNull(response.body().getToken());
        assertEquals("loginTest", response.body().getUsername());

        userRepository.findByEmail("login@integration.com")
                .ifPresent(userRepository::delete);
    }

    @Test
    void testLogin_WrongPassword() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("wrongpass");
        registerRequest.setEmail("wrongpass@integration.com");
        registerRequest.setPassword("correctpass");
        registerRequest.setFullName("Wrong Pass Test");
        registerRequest.setRole(UserRole.CUSTOMER);

        client.toBlocking().exchange(
                HttpRequest.POST("/api/auth/register", registerRequest),
                AuthResponse.class
        );

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("wrongpass");
        loginRequest.setPassword("wrongpassword");

        try {
            client.toBlocking().exchange(
                    HttpRequest.POST("/api/auth/login", loginRequest),
                    AuthResponse.class
            );
            fail("Should have returned 401");
        } catch (Exception e) {
            // Exception
        } finally {
            userRepository.findByEmail("wrongpass@integration.com")
                    .ifPresent(userRepository::delete);
        }
    }

}
