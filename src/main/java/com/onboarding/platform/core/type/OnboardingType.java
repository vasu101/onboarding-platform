package com.onboarding.platform.core.type;

/**
 * Defines the type of subject being onboarded.
 */
public enum OnboardingType {
    INDIVIDUAL("Individual", "Personal onboarding for individual users"),
    BUSINESS("Business", "Onboarding for business entities"),
    PARTNER("Partner", "Onboarding for business partners"),
    VENDOR("Vendor", "Onboarding for vendors/suppliers");

    private final String displayName;
    private final String description;

    OnboardingType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean requiresEnhancedVerification() {
        return this == BUSINESS || this == PARTNER;
    }
}
