package com.onboarding.platform.api.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class ApprovalRequest {

    private String comments;

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
