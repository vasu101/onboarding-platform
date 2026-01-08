package com.onboarding.platform.core.exception;

/**
 * Thrown when correction attempts exceed the maximum allowed
 */
public class MaxCorrectionAttemptsExceededException extends OnboardingException {

    private final int attempts;
    private final int maxAttempts;

    public MaxCorrectionAttemptsExceededException(int attempts, int maxAttempts) {
        super(String.format("Maximum correction attempts (%d) exceeded. Current: %d", maxAttempts, attempts));
        this.attempts = attempts;
        this.maxAttempts = maxAttempts;
    }

    public int getAttempts() {
        return attempts;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }
}
