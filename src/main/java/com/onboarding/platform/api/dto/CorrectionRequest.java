package com.onboarding.platform.api.dto;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Serdeable
public class CorrectionRequest {

    @NotBlank
    private String comments;

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
