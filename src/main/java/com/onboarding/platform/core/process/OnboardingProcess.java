package com.onboarding.platform.core.process;

import com.onboarding.platform.core.state.OnboardingState;
import com.onboarding.platform.core.subject.OnboardingSubject;
import io.micronaut.data.annotation.*;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.Version;
import io.micronaut.serde.annotation.Serdeable;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents the onboarding process/workflow instance.
 * Tracks state, attempts, and workflow metadata.
 */
@Entity
@Table(name = "onboarding_processes")
@Serdeable
public class OnboardingProcess {

    @Id
    @GeneratedValue
    private UUID id;

    // Link to subject
    @ManyToOne(optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private OnboardingSubject subject;

    // Current state
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OnboardingState currentState;

    // Workflow tracking
    @Column(nullable = false)
    private Integer correctionAttempts = 0;

    @Column(nullable = false)
    private Integer maxCorrectionAttempts = 3;

    @Column(length = 1000)
    private String correctionComments;

    // Verification tracking
    @Column
    private Boolean verificationPassed;

    @Column(length = 1000)
    private String verificationDetails;

    // Approval tracking
    @Column
    private String approvedBy;

    @Column
    private Instant approvedAt;

    @Column(length = 1000)
    private String approvalComments;

    @Column
    private String rejectedBy;

    @Column
    private Instant rejectedAt;

    @Column(length = 1000)
    private String rejectionReason;

    // Timestamps
    @Column
    private Instant submittedAt;

    @Column
    private Instant completedAt;

    @Column
    private Instant cancelledAt;

    @Column
    private String cancelledBy;

    @Column(length = 500)
    private String cancellationReason;

    // Metadata
    @DateCreated
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @DateUpdated
    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    public OnboardingProcess() {
    }

    public OnboardingProcess(OnboardingSubject subject) {
        this.subject = subject;
        this.currentState = OnboardingState.DRAFT;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OnboardingSubject getSubject() {
        return subject;
    }

    public void setSubject(OnboardingSubject subject) {
        this.subject = subject;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // Business methods
    public boolean canRequestCorrection() {
        return correctionAttempts < maxCorrectionAttempts;
    }

    public void incrementCorrectionAttempts() {
        this.correctionAttempts++;
    }

    public boolean isInTerminalState() {
        return currentState.isTerminal();
    }

    public boolean canBeEdited() {
        return currentState.isEditable();
    }
}