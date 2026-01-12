package com.onboarding.platform.api.dto;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;

@Serdeable
public class VerificationRequest {

    @NotNull
    private Boolean passed;

    private String details;

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
