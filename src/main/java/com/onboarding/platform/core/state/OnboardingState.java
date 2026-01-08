package com.onboarding.platform.core.state;

/**
 * Represents all possible states in the onboarding lifecycle.
 */
public enum OnboardingState {

    // Initial state
    DRAFT("Draft", "Onboarding created but not submitted"),

    // Submitted and under review
    SUBMITTED("Submitted", "Onboarding submitted for review"),

    // Correction flow
    PENDING_CORRECTION("Pending Correction", "Requires corrections from submitter"),
    CORRECTED("Corrected", "Corrections made, awaiting re-review"),

    // Verification states
    VERIFICATION_IN_PROGRESS("Verification In Progress", "Documents are being verified"),
    VERIFICATION_FAILED("Verification Failed", "Verification checks failed"),

    // Approval states
    PENDING_APPROVAL("Pending Approval", "Awaiting manager approval"),
    APPROVED("Approved", "Onboarding approved"),

    // Final states
    REJECTED("Rejected", "Onboarding rejected after multiple attempts"),
    COMPLETED("Completed", "Onboarding process completed successfully"),
    CANCELLED("Cancelled", "Onboarding process cancelled by user/admin");

    private final String displayName;
    private final String description;

    OnboardingState(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED || this == REJECTED;
    }

    public boolean isEditable() {
        return this == DRAFT || this == PENDING_CORRECTION;
    }

    public boolean requiresSubmitterAction() {
        return this == DRAFT || this == PENDING_CORRECTION || this == VERIFICATION_FAILED;
    }

    public boolean isUnderReview() {
        return this == SUBMITTED || this == CORRECTED ||
                this == VERIFICATION_IN_PROGRESS || this == PENDING_APPROVAL;
    }
}
