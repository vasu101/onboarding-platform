package com.onboarding.platform.security.model;

/**
 * User roles in the system
 */
public enum UserRole {

    CUSTOMER("Customer", "Can create and manage own onboardings"),
    REVIEWER("Reviewer", "Can review onboardings and request corrections"),
    APPROVER("Approver", "Can approve or reject onboardings"),
    ADMIN("Admin", "Full system access");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean canCreateOnboarding() {
        return this == CUSTOMER || this == ADMIN;
    }

    public boolean canReview() {
        return this == REVIEWER || this == APPROVER || this == ADMIN;
    }
    public boolean canApprove() {
        return this == APPROVER || this == ADMIN;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }
}
