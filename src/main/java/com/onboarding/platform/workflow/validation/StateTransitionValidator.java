package com.onboarding.platform.workflow.validation;

import com.onboarding.platform.core.exception.InvalidStateTransitionException;
import com.onboarding.platform.core.state.OnboardingState;
import jakarta.inject.Singleton;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Validates state transitions
 * Defines the state machine for onboarding workflow
 */
@Singleton
public class StateTransitionValidator {

    private final Map<OnboardingState, Set<OnboardingState>> validTransitions;

    public StateTransitionValidator() {
        validTransitions = new EnumMap<>(OnboardingState.class);
        initializeTransitions();
    }

    private void initializeTransitions() {
        // DRAFT can go to SUBMITTED or CANCELLED
        validTransitions.put(OnboardingState.DRAFT, EnumSet.of(
                OnboardingState.SUBMITTED,
                OnboardingState.CANCELLED
        ));

        // SUBMITTED can go to verification, correction, or cancellation
        validTransitions.put(OnboardingState.SUBMITTED, EnumSet.of(
                OnboardingState.VERIFICATION_IN_PROGRESS,
                OnboardingState.PENDING_CORRECTION,
                OnboardingState.CANCELLED
        ));

        // PENDING_CORRECTION can be edited and resubmitted or canceled
        validTransitions.put(OnboardingState.PENDING_CORRECTION, EnumSet.of(
                OnboardingState.CORRECTED,
                OnboardingState.CANCELLED
        ));

        // CORRECTED goes back to review or rejection
        validTransitions.put(OnboardingState.CORRECTED, EnumSet.of(
                OnboardingState.VERIFICATION_IN_PROGRESS,
                OnboardingState.PENDING_CORRECTION,
                OnboardingState.CANCELLED,
                OnboardingState.REJECTED
        ));

        // VERIFICATION_IN_PROGRESS can pass or fail
        validTransitions.put(OnboardingState.VERIFICATION_IN_PROGRESS, EnumSet.of(
                OnboardingState.PENDING_APPROVAL,
                OnboardingState.VERIFICATION_FAILED,
                OnboardingState.CANCELLED
        ));

        // VERIFICATION_FAILED can be corrected or canceled
        validTransitions.put(OnboardingState.VERIFICATION_FAILED, EnumSet.of(
                OnboardingState.CORRECTED,
                OnboardingState.CANCELLED
        ));

        // PENDING_APPROVAL can be approved, rejected, or canceled
        validTransitions.put(OnboardingState.PENDING_APPROVAL, EnumSet.of(
                OnboardingState.APPROVED,
                OnboardingState.REJECTED,
                OnboardingState.CANCELLED
        ));

        // APPROVED moves to completion
        validTransitions.put(OnboardingState.APPROVED, EnumSet.of(
                OnboardingState.COMPLETED
        ));

        // Terminal states have no transitions
        validTransitions.put(OnboardingState.REJECTED, EnumSet.noneOf(OnboardingState.class));
        validTransitions.put(OnboardingState.COMPLETED, EnumSet.noneOf(OnboardingState.class));
        validTransitions.put(OnboardingState.CANCELLED, EnumSet.noneOf(OnboardingState.class));
    }

    /**
     * Validate if transition from current state to new state is allowed
     */
    public void validateTransition(OnboardingState currentState, OnboardingState newState) {
        if(currentState == newState) {
            return;
        }

        Set<OnboardingState> allowedStates = validTransitions.get(currentState);

        if(allowedStates == null || !allowedStates.contains(newState)) {
            throw new InvalidStateTransitionException(currentState, newState);
        }
    }

    /**
     * Check if state is terminal
     */
    public boolean isTerminalState(OnboardingState currentState) {
        Set<OnboardingState> allowedStates = validTransitions.get(currentState);
        return allowedStates == null || allowedStates.isEmpty();
    }

    /**
     * Get all valid next states from current state
     */
    public Set<OnboardingState> getValidNextStates(OnboardingState currentState) {
        Set<OnboardingState> states = validTransitions.get(currentState);
        return states != null ? EnumSet.copyOf(states) : EnumSet.noneOf(OnboardingState.class);
    }
}
