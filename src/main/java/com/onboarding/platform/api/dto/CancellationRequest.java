package com.onboarding.platform.api.dto;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Serdeable
public class CancellationRequest {

    @NotBlank
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
