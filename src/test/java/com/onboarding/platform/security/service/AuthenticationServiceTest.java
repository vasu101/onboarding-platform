package com.onboarding.platform.security.service;

import com.onboarding.platform.security.model.User;
import com.onboarding.platform.security.model.UserRole;
import com.onboarding.platform.security.repository.UserRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticationService
 */
@MicronautTest
public class AuthenticationServiceTest {

    private AuthenticationService authenticationService;
    private UserRepository userRepository;
    private PasswordEncoderService encoderService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        encoderService = new PasswordEncoderService();
        authenticationService = new AuthenticationService(userRepository, encoderService);
    }

    @Test
    void testRegisterSuccess() {
        when(userRepository.existsByEmail("newUser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        User result = authenticationService.register(
                "newUser",
                "new@example.com",
                "password123",
                "New User",
                UserRole.CUSTOMER
        );

        assertNotNull(result);
        assertEquals("newUser", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        assertEquals(UserRole.CUSTOMER, result.getRole());
        assertTrue(result.getActive());
        assertFalse(result.getEmailVerified());
        verify(userRepository).save(any());
    }

    @Test
    void testRegister_DuplicateUsername() {
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> authenticationService.register("existing", "new@example.com", "pass", "User", UserRole.CUSTOMER));

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testAuthenticateSuccess() {
        String password = "password123";
        String hashedPassword = encoderService.encode(password);

        User user = createTestUser(hashedPassword);
        when(userRepository.findByUsernameAndActiveTrue("testUser")).thenReturn(Optional.of(user));
        when(userRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<User> result = authenticationService.authenticate("testUser", password);

        assertTrue(result.isPresent());
        assertEquals("testUser", result.get().getUsername());
        verify(userRepository).update(any());
    }

    @Test
    void testAuthenticateFailure_WrongPassword() {
        String correctPassword = "password123";
        String hashedPassword = encoderService.encode(correctPassword);

        User user = createTestUser(hashedPassword);
        when(userRepository.findByUsernameAndActiveTrue("testUser")).thenReturn(Optional.of(user));

        Optional<User> result = authenticationService.authenticate("testUser", "wrongPassword");

        assertFalse(result.isPresent());
        verify(userRepository, never()).update(any());
    }

    @Test
    void testAuthenticateFailure_UserNotFound() {
        when(userRepository.findByUsernameAndActiveTrue("nonexistent")).thenReturn(Optional.empty());

        Optional<User> result = authenticationService.authenticate("nonexistent", "password");

        assertFalse(result.isPresent());
    }

    @Test
    void testChangePassword() {
        String oldPassword = "oldPass123";
        String newPassword = "newPass456";
        String hashedOldPassword = encoderService.encode(oldPassword);

        User user = createTestUser(hashedOldPassword);
        when(userRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        authenticationService.changePassword(user, oldPassword, newPassword);

        verify(userRepository).update(user);
        assertTrue(encoderService.matches(newPassword, user.getPasswordHash()));
    }

    @Test
    void testChangePassword_WrongOldPassword() {
        String oldPassword = "oldPass123";
        String hashedOldPassword = encoderService.encode(oldPassword);

        User user = createTestUser(hashedOldPassword);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> authenticationService.changePassword(user, "wrongPass", "newPass"));

        assertEquals("Current password is incorrect", exception.getMessage());
        verify(userRepository, never()).update(any());
    }

    // Helper method

    private User createTestUser(String passwordHash) {
        User user = new User();
        user.setUsername("testUser");
        user.setEmail("testUser" + "@example.com");
        user.setPasswordHash(passwordHash);
        user.setFullName("Test User");
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
        return user;
    }
}
