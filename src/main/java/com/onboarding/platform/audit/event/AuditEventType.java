package com.onboarding.platform.audit.event;

/**
 * Types of auditable events in the onboarding system
 */
public enum AuditEventType {

    // Process lifecycle
    PROCESS_CREATED("Process Created"),
    PROCESS_UPDATED("Process Updated"),
    PROCESS_DELETED("Process Deleted"),

    // State transitions
    STATE_CHANGED("State Changed"),
    SUBMITTED("Submitted for Review"),
    CORRECTION_REQUESTED("Correction Requested"),
    CORRECTED("Corrections Submitted"),

    // Verification
    VERIFICATION_STARTED("Verification Started"),
    VERIFICATION_PASSED("Verification Passed"),
    VERIFICATION_FAILED("Verification Failed"),

    // Approval workflow
    APPROVAL_REQUESTED("Approval Requested"),
    APPROVED("Approved"),
    REJECTED("Rejected"),

    // Completion
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),

    // Subject changes
    SUBJECT_CREATED("Subject Created"),
    SUBJECT_UPDATED("Subject Updated"),

    // Security events
    ACCESS_GRANTED("Access Granted"),
    ACCESS_DENIED("Access Denied"),
    UNAUTHORIZED_ACCESS_ATTEMPT("Unauthorized Access Attempt"),

    // System events
    NOTIFICATION_SENT("Notification Sent"),
    DOCUMENT_UPLOADED("Document Uploaded"),
    DOCUMENT_DELETED("Document Deleted"),

    // Error events
    SYSTEM_ERROR("System Error"),
    VALIDATION_ERROR("Validation Error");

    private final String displayName;

    AuditEventType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if this event type represents a state transition
     */
    public boolean isStateTransition() {
        return this == STATE_CHANGED ||
                this == SUBMITTED ||
                this == CORRECTED ||
                this == APPROVED ||
                this == REJECTED ||
                this == COMPLETED ||
                this == CANCELLED;
    }

    /**
     * Check if this is a security-related event
     */
    public boolean isSecurityEvent() {
        return this == ACCESS_GRANTED ||
                this == ACCESS_DENIED ||
                this == UNAUTHORIZED_ACCESS_ATTEMPT;
    }
}
