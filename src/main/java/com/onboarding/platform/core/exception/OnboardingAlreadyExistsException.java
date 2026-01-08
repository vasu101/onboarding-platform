package com.onboarding.platform.core.exception;

/**
 * Thrown when attempting to create duplicate onboarding for same subject
 */
public class OnboardingAlreadyExistsException extends OnboardingException {

    private final String email;

    public OnboardingAlreadyExistsException(String email) {
        super(String.format("Active onboarding already exists for email: %s", email));
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
