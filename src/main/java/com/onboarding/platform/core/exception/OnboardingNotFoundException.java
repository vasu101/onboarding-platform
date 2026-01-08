package com.onboarding.platform.core.exception;

import java.util.UUID;

/**
 * Thrown when an onboarding process is not found
 */
public class OnboardingNotFoundException extends OnboardingException {

    private final UUID processId;

    public OnboardingNotFoundException(UUID processId) {
        super(String.format("Onboarding process not found: %s", processId));
        this.processId = processId;
    }

    public UUID getProcessId() {
        return processId;
    }
}
