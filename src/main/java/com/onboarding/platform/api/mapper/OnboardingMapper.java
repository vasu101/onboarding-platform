package com.onboarding.platform.api.mapper;

import com.onboarding.platform.api.dto.CreateOnboardingRequest;
import com.onboarding.platform.api.dto.OnboardingResponse;
import com.onboarding.platform.core.process.OnboardingProcess;
import com.onboarding.platform.core.subject.OnboardingSubject;
import jakarta.inject.Singleton;

/**
 * Maps between domain entities and API DTOs
 */
@Singleton
public class OnboardingMapper {

    /**
     * Convert CreateOnboardingRequest to OnboardingSubject entity
     */
    public OnboardingSubject toSubjectEntity(CreateOnboardingRequest request) {
        OnboardingSubject subject = new OnboardingSubject();
        subject.setType(request.getType());
        subject.setFullName(request.getFullName());
        subject.setEmail(request.getEmail());
        subject.setPhoneNumber(request.getPhoneNumber());
        subject.setBusinessName(request.getBusinessName());
        subject.setTaxId(request.getTaxId());
        subject.setRegistrationNumber(request.getRegistrationNumber());
        subject.setAddress(request.getAddress());
        subject.setCity(request.getCity());
        subject.setCountry(request.getCountry());
        subject.setPostalCode(request.getPostalCode());
        return subject;
    }

    /**
     * Convert OnboardingProcess to OnboardingResponse DTO
     */
    public OnboardingResponse toResponse(OnboardingProcess process) {
        OnboardingResponse response = new OnboardingResponse();

        // Process info
        response.setId(process.getId());
        response.setCurrentState(process.getCurrentState());
        response.setCorrectionAttempts(process.getCorrectionAttempts());
        response.setMaxCorrectionAttempts(process.getMaxCorrectionAttempts());
        response.setCorrectionComments(process.getCorrectionComments());

        // Verification info
        response.setVerificationPassed(process.getVerificationPassed());
        response.setVerificationDetails(process.getVerificationDetails());

        // Approval info
        response.setApprovedBy(process.getApprovedBy());
        response.setApprovedAt(process.getApprovedAt());
        response.setApprovalComments(process.getApprovalComments());

        // Rejection info
        response.setRejectedBy(process.getRejectedBy());
        response.setRejectedAt(process.getRejectedAt());
        response.setRejectionReason(process.getRejectionReason());

        // Timestamps
        response.setSubmittedAt(process.getSubmittedAt());
        response.setCompletedAt(process.getCompletedAt());
        response.setCancelledAt(process.getCancelledAt());
        response.setCancelledBy(process.getCancelledBy());
        response.setCancellationReason(process.getCancellationReason());
        response.setCreatedAt(process.getCreatedAt());
        response.setUpdatedAt(process.getUpdatedAt());

        // Subject info
        if (process.getSubject() != null) {
            response.setSubjectId(process.getSubject().getId());
            response.setSubjectName(process.getSubject().getDisplayName());
            response.setSubjectEmail(process.getSubject().getEmail());
            response.setType(process.getSubject().getType());
        }

        return response;
    }
}
