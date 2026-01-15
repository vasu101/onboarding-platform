package com.onboarding.platform.api.controller;

import com.onboarding.platform.api.dto.CreateOnboardingRequest;
import com.onboarding.platform.api.dto.OnboardingResponse;
import com.onboarding.platform.api.dto.auth.AuthResponse;
import com.onboarding.platform.api.dto.auth.LoginRequest;
import com.onboarding.platform.core.process.OnboardingProcessRepository;
import com.onboarding.platform.core.state.OnboardingState;
import com.onboarding.platform.core.subject.OnboardingSubjectRepository;
import com.onboarding.platform.core.type.OnboardingType;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for OnboardingController
 */
@MicronautTest
public class OnboardingControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    OnboardingProcessRepository processRepository;

    @Inject
    OnboardingSubjectRepository subjectRepository;

    private String authToken;

    @BeforeEach
    void setUp() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin@25");

        HttpResponse<AuthResponse> loginResponse = client.toBlocking().exchange(
                HttpRequest.POST("/api/auth/login", loginRequest),
                AuthResponse.class
        );

        authToken = loginResponse.body().getToken();
    }

    @AfterEach
    void cleanUp() {
        processRepository.deleteAll();
        subjectRepository.findAll().stream()
                .filter(s -> s.getEmail().contains("controller-test"))
                .forEach(subjectRepository::delete);
    }

    @Test
    void testCreateOnboarding() {
        CreateOnboardingRequest request = new CreateOnboardingRequest();
        request.setType(OnboardingType.INDIVIDUAL);
        request.setFullName("Test User");
        request.setEmail("controller-test@example.com");
        request.setPhoneNumber("+1234567890");
        request.setCountry("USA");

        HttpResponse<OnboardingResponse> response = client.toBlocking().exchange(
                HttpRequest.POST("/api/onboarding", request)
                        .bearerAuth(authToken),
                OnboardingResponse.class
        );

        assertEquals(HttpStatus.CREATED, response.status());
        assertNotNull(response.body());
        assertEquals(OnboardingState.DRAFT, response.body().getCurrentState());
        assertEquals("Test User", response.body().getSubjectName());
    }

    @Test
    void testCreateOnboarding_Unauthorized() {
        CreateOnboardingRequest request = new CreateOnboardingRequest();
        request.setType(OnboardingType.INDIVIDUAL);
        request.setFullName("Test User");
        request.setEmail("unauthorized@example.com");

        try {
            client.toBlocking().exchange(
                    HttpRequest.POST("/api/onboarding", request),
                    OnboardingResponse.class
            );
            fail("Should have returned 401");
        } catch (Exception e) {
            // Expected 401 Unauthorized
        }
    }

    @Test
    void testGetOnboarding() {
        CreateOnboardingRequest createRequest = new CreateOnboardingRequest();
        createRequest.setType(OnboardingType.INDIVIDUAL);
        createRequest.setFullName("Get Test");
        createRequest.setEmail("get-test@example.com");

        HttpResponse<OnboardingResponse> createResponse = client.toBlocking().exchange(
                HttpRequest.POST("/api/onboarding", createRequest)
                        .bearerAuth(authToken),
                OnboardingResponse.class
        );

        String onboardingId = createResponse.body().getId().toString();

        HttpResponse<OnboardingResponse> getResponse = client.toBlocking().exchange(
                HttpRequest.GET("/api/onboarding/" + onboardingId)
                        .bearerAuth(authToken),
                OnboardingResponse.class
        );

        assertEquals(HttpStatus.OK, getResponse.status());
        assertNotNull(getResponse.body());
        assertEquals(onboardingId, getResponse.body().getId().toString());
        assertEquals("Get Test", getResponse.body().getSubjectName());
    }

    @Test
    void testSubmitOnboarding() {
        CreateOnboardingRequest createRequest = new CreateOnboardingRequest();
        createRequest.setType(OnboardingType.INDIVIDUAL);
        createRequest.setFullName("Submit Test");
        createRequest.setEmail("submit-test@example.com");

        HttpResponse<OnboardingResponse> createResponse = client.toBlocking().exchange(
                HttpRequest.POST("/api/onboarding", createRequest)
                        .bearerAuth(authToken),
                OnboardingResponse.class
        );

        String onboardingId = createResponse.body().getId().toString();

        HttpResponse<OnboardingResponse> submitResponse = client.toBlocking().exchange(
                HttpRequest.POST("/api/onboarding/" + onboardingId + "/submit", null)
                        .bearerAuth(authToken),
                OnboardingResponse.class
        );

        assertEquals(HttpStatus.OK, submitResponse.status());
        assertNotNull(submitResponse.body());
        assertEquals(OnboardingState.SUBMITTED, submitResponse.body().getCurrentState());
        assertNotNull(submitResponse.body().getSubmittedAt());
    }

    @Test
    void testGetAllOnboardings() {
        for (int i = 0; i < 3; i++) {
            CreateOnboardingRequest request = new CreateOnboardingRequest();
            request.setType(OnboardingType.INDIVIDUAL);
            request.setFullName("User " + i);
            request.setEmail("list-test-" + i + "@example.com");

            client.toBlocking().exchange(
                    HttpRequest.POST("/api/onboarding", request)
                            .bearerAuth(authToken),
                    OnboardingResponse.class
            );
        }

        HttpResponse<OnboardingResponse[]> response = client.toBlocking().exchange(
                HttpRequest.GET("/api/onboarding")
                        .bearerAuth(authToken),
                OnboardingResponse[].class
        );

        assertEquals(HttpStatus.OK, response.status());
        assertNotNull(response.body());
        assertTrue(response.body().length >= 3);
    }

    @Test
    void testGetOnboarding_NotFound() {
        String fakeId = "00000000-0000-0000-0000-000000000000";

        try {
            client.toBlocking().exchange(
                    HttpRequest.GET("/api/onboarding/" + fakeId)
                            .bearerAuth(authToken),
                    OnboardingResponse.class
            );
            fail("Should have returned 404");
        } catch (Exception e) {
            // Expected 404 Not Found
        }
    }
}
