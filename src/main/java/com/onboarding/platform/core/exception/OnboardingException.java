package com.onboarding.platform.core.exception;

/**
 * Base exception for all onboarding domain errors
 */
public class OnboardingException extends RuntimeException {

    public OnboardingException(String message) {
        super(message);
    }

    public OnboardingException(String message, Throwable cause) {
        super(message, cause);
    }
}

