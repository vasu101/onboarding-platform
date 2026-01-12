package com.onboarding.platform.api.dto;

import com.onboarding.platform.core.state.OnboardingState;
import com.onboarding.platform.core.type.OnboardingType;
import io.micronaut.serde.annotation.Serdeable;

import java.time.Instant;
import java.util.UUID;

@Serdeable
public class OnboardingResponse {

    private UUID id;
    private UUID subjectId;
    private String subjectName;
    private String subjectEmail;
    private OnboardingType type;
    private OnboardingState currentState;
    private Integer correctionAttempts;
    private Integer maxCorrectionAttempts;
    private String correctionComments;
    private Boolean verificationPassed;
    private String verificationDetails;
    private String approvedBy;
    private Instant approvedAt;
    private String approvalComments;
    private String rejectedBy;
    private Instant rejectedAt;
    private String rejectionReason;
    private Instant submittedAt;
    private Instant completedAt;
    private Instant cancelledAt;
    private String cancelledBy;
    private String cancellationReason;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(UUID subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectEmail() {
        return subjectEmail;
    }

    public void setSubjectEmail(String subjectEmail) {
        this.subjectEmail = subjectEmail;
    }

    public OnboardingType getType() {
        return type;
    }

    public void setType(OnboardingType type) {
        this.type = type;
    }

    public OnboardingState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(OnboardingState currentState) {
        this.currentState = currentState;
    }

    public Integer getCorrectionAttempts() {
        return correctionAttempts;
    }

    public void setCorrectionAttempts(Integer correctionAttempts) {
        this.correctionAttempts = correctionAttempts;
    }

    public Integer getMaxCorrectionAttempts() {
        return maxCorrectionAttempts;
    }

    public void setMaxCorrectionAttempts(Integer maxCorrectionAttempts) {
        this.maxCorrectionAttempts = maxCorrectionAttempts;
    }

    public String getCorrectionComments() {
        return correctionComments;
    }

    public void setCorrectionComments(String correctionComments) {
        this.correctionComments = correctionComments;
    }

    public Boolean getVerificationPassed() {
        return verificationPassed;
    }

    public void setVerificationPassed(Boolean verificationPassed) {
        this.verificationPassed = verificationPassed;
    }

    public String getVerificationDetails() {
        return verificationDetails;
    }

    public void setVerificationDetails(String verificationDetails) {
        this.verificationDetails = verificationDetails;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getApprovalComments() {
        return approvalComments;
    }

    public void setApprovalComments(String approvalComments) {
        this.approvalComments = approvalComments;
    }

    public String getRejectedBy() {
        return rejectedBy;
    }

    public void setRejectedBy(String rejectedBy) {
        this.rejectedBy = rejectedBy;
    }

    public Instant getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(Instant rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(String cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
