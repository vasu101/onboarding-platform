package com.onboarding.platform.core.exception;

import com.onboarding.platform.core.state.OnboardingState;

/**
 * Thrown when an invalid state transition is attempted
 */
public class InvalidStateTransitionException extends OnboardingException {

    private final OnboardingState fromState;
    private final OnboardingState toState;

    public InvalidStateTransitionException(OnboardingState fromState, OnboardingState toState) {
        super(String.format("Invalid state transition from %s to %s", fromState, toState));
        this.fromState = fromState;
        this.toState = toState;
    }

    public OnboardingState getFromState() {
        return fromState;
    }

    public OnboardingState getToState() {
        return toState;
    }
}
