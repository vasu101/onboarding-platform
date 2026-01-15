package com.onboarding.platform.security.repository;

import com.onboarding.platform.security.model.User;
import com.onboarding.platform.security.model.UserRole;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Repository tests or User
 */
@MicronautTest(transactional = false)
public class UserRepositoryTest {

    @Inject
    UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.findAll().stream()
                .filter(u -> u.getEmail().contains("test"))
                .forEach(userRepository::delete);
    }

    @Test
    void testSaveAndFindByUsername() {
        User user = createTestUser("testUser", "test@example.com");

        User saved = userRepository.save(user);
        Optional<User> found = userRepository.findByUsername("testUser");

        assertTrue(found.isPresent());
        assertEquals("testUser", found.get().getUsername());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void testFindByRole() {
        User customer = createTestUser("customer1", "customer1@example.com");
        customer.setRole(UserRole.CUSTOMER);
        userRepository.save(customer);

        User reviewer = createTestUser("reviewer1", "reviewer1@example.com");
        reviewer.setRole(UserRole.REVIEWER);
        userRepository.save(reviewer);

        List<User> customers = userRepository.findByRole(UserRole.CUSTOMER);
        List<User> reviewers = userRepository.findByRole(UserRole.REVIEWER);

        assertTrue(customers.stream().anyMatch(u -> u.getUsername().equals("customer1")));
        assertTrue(reviewers.stream().anyMatch(u -> u.getUsername().equals("reviewer1")));
    }

    @Test
    void testUniqueConstraints() {
        User user1 = createTestUser("duplicate", "duplicate@example.com");
        userRepository.save(user1);

        User user2 = createTestUser("duplicate", "different@example.com");
        assertThrows(Exception.class, () -> userRepository.save(user2));
    }

    // Helper methods

    private User createTestUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash("hashed_password");
        user.setFullName("Test User");
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);
        return user;
    }
}
