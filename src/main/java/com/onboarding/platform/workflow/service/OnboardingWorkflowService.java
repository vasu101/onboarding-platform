package com.onboarding.platform.workflow.service;

import com.onboarding.platform.audit.event.AuditEventType;
import com.onboarding.platform.audit.service.AuditService;
import com.onboarding.platform.core.exception.InvalidStateTransitionException;
import com.onboarding.platform.core.exception.MaxCorrectionAttemptsExceededException;
import com.onboarding.platform.core.exception.OnboardingAlreadyExistsException;
import com.onboarding.platform.core.exception.OnboardingNotFoundException;
import com.onboarding.platform.core.process.OnboardingProcess;
import com.onboarding.platform.core.process.OnboardingProcessRepository;
import com.onboarding.platform.core.state.OnboardingState;
import com.onboarding.platform.core.subject.OnboardingSubject;
import com.onboarding.platform.core.subject.OnboardingSubjectRepository;
import com.onboarding.platform.workflow.validation.StateTransitionValidator;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

/**
 * Core workflow service that orchestrate the onboarding process.
 * Handles state transitions, validation, and coordination with other services.
 */
@Singleton
public class OnboardingWorkflowService {

    private static final Logger LOG = LoggerFactory.getLogger(OnboardingWorkflowService.class);

    private final OnboardingProcessRepository processRepository;
    private final OnboardingSubjectRepository subjectRepository;
    private final StateTransitionValidator stateValidator;
    private final AuditService auditService;

    public OnboardingWorkflowService(
            OnboardingProcessRepository processRepository,
            OnboardingSubjectRepository subjectRepository,
            StateTransitionValidator stateValidator,
            AuditService auditService
    ) {
        this.processRepository = processRepository;
        this.subjectRepository = subjectRepository;
        this.stateValidator = stateValidator;
        this.auditService = auditService;
    }

    @Transactional
    public OnboardingProcess createOnboarding(OnboardingSubject subject, String createdBy) {
        LOG.info("Creating onboarding for subject: {}", subject.getEmail());

        if(subjectRepository.existsByEmail(subject.getEmail())) {
            OnboardingSubject existing = subjectRepository.findByEmail(subject.getEmail())
                    .orElseThrow();
            if(processRepository.hasActiveOnboarding(existing.getId())) {
                throw new OnboardingAlreadyExistsException(subject.getEmail());
            }
        }

        subject.setCreatedBy(createdBy);
        OnboardingSubject savedSubject = subjectRepository.save(subject);

        OnboardingProcess process = new OnboardingProcess(savedSubject);
        OnboardingProcess savedProcess = processRepository.save(process);

        // Audit
        auditService.logEvent(
                savedProcess.getId(),
                savedSubject.getEmail(),
                AuditEventType.PROCESS_CREATED,
                createdBy,
                "Onboarding process created in DRAFT state",
                null
        );

        LOG.info("Onboarding created: {} for {}", savedProcess.getId(), savedSubject.getEmail());
        return savedProcess;
    }

    @Transactional
    public OnboardingProcess submitForReview(UUID processId, String submittedBy) {
        LOG.info("Submitting onboarding {} for review", processId);

        OnboardingProcess process = getProcess(processId);

        // Validate transition
        stateValidator.validateTransition(process.getCurrentState(), OnboardingState.SUBMITTED);

        // Update process
        OnboardingState previousState = process.getCurrentState();
        process.setCurrentState(OnboardingState.SUBMITTED);
        process.setSubmittedAt(Instant.now());

        OnboardingProcess updated = processRepository.update(process);

        // Audit
        auditService.logSubmission(updated, submittedBy);
        auditService.logStateTransition(updated, previousState, OnboardingState.SUBMITTED, submittedBy, null);

        LOG.info("Onboarding {} submitted successfully", processId);
        return updated;
    }

    /**
     * Request corrections from submitter
     */
    @Transactional
    public OnboardingProcess requestCorrection(UUID processId, String comments, String requestedBy) {
        LOG.info("Requesting correction for onboarding {}", processId);

        OnboardingProcess process = getProcess(processId);

        // Check correction attempts
        if(!process.canRequestCorrection()) {
            throw new MaxCorrectionAttemptsExceededException(
                    process.getCorrectionAttempts(),
                    process.getMaxCorrectionAttempts()
            );
        }

        // Validate transition
        stateValidator.validateTransition(process.getCurrentState(), OnboardingState.PENDING_CORRECTION);

        // Update transition
        OnboardingState previousState = process.getCurrentState();
        process.setCurrentState(OnboardingState.PENDING_CORRECTION);
        process.incrementCorrectionAttempts();
        process.setCorrectionComments(comments);

        OnboardingProcess updated = processRepository.update(process);

        // Audit
        auditService.logCorrectionRequest(updated, comments, requestedBy);
        auditService.logStateTransition(updated, previousState, OnboardingState.PENDING_CORRECTION, requestedBy, "Correction requested");

        LOG.info("Corrections requested for onboarding {}, attempt {}/{}", processId, updated.getCorrectionAttempts(), updated.getMaxCorrectionAttempts());
        return updated;
    }

    @Transactional
    public OnboardingProcess submitCorrections(UUID processId, String submittedBy) {
        LOG.info("Submitting corrections for onboarding {}", processId);

        OnboardingProcess process = getProcess(processId);

        // Validate current state
        if(process.getCurrentState() != OnboardingState.PENDING_CORRECTION) {
            throw new InvalidStateTransitionException(process.getCurrentState(), OnboardingState.CORRECTED);
        }

        // Update state
        OnboardingState previousState = process.getCurrentState();
        process.setCurrentState(OnboardingState.CORRECTED);
        process.setCorrectionComments(null);

        OnboardingProcess updated = processRepository.update(process);

        // Audit
        auditService.logEvent(
                updated.getId(),
                updated.getSubject().getEmail(),
                AuditEventType.CORRECTED,
                submittedBy,
                "Corrections submitted for re-review",
                null
        );
        auditService.logStateTransition(updated, previousState, OnboardingState.CORRECTED, submittedBy, null);

        LOG.info("Corrections submitted for onboarding {}", processId);
        return updated;
    }

    @Transactional
    public OnboardingProcess startVerification(UUID processId, String startedBy) {
        LOG.info("Starting verification for onboarding {}", processId);

        OnboardingProcess process = getProcess(processId);

        // Validate transition
        stateValidator.validateTransition(process.getCurrentState(), OnboardingState.VERIFICATION_IN_PROGRESS);

        // Update state
        OnboardingState previousState = process.getCurrentState();
        process.setCurrentState(OnboardingState.VERIFICATION_IN_PROGRESS);
        OnboardingProcess updated = processRepository.update(process);

        // Audit
        auditService.logEvent(
                updated.getId(),
                updated.getSubject().getEmail(),
                AuditEventType.VERIFICATION_STARTED,
                startedBy,
                "Verification process started",
                null
        );
        auditService.logStateTransition(updated, previousState, OnboardingState.VERIFICATION_IN_PROGRESS, startedBy, null);

        LOG.info("Verification started for onboarding {}", processId);
        return updated;
    }

    @Transactional
    public OnboardingProcess completeVerification(UUID processId, boolean passed, String details, String performedBy) {
        LOG.info("Completing verification for onboarding {}: {}", processId, passed ? "PASSED" : "FAILED");

        OnboardingProcess process = getProcess(processId);

        // Validate current state
        if(process.getCurrentState() != OnboardingState.VERIFICATION_IN_PROGRESS) {
            throw new InvalidStateTransitionException(process.getCurrentState(),
                    passed ? OnboardingState.PENDING_APPROVAL : OnboardingState.VERIFICATION_FAILED);
        }

        // Update process
        OnboardingState previousState = process.getCurrentState();
        OnboardingState newState = passed ? OnboardingState.PENDING_APPROVAL : OnboardingState.VERIFICATION_FAILED;

        process.setCurrentState(newState);
        process.setVerificationPassed(passed);
        process.setVerificationDetails(details);

        OnboardingProcess updated = processRepository.update(process);

        // Audit
        auditService.logVerification(updated, passed, details, performedBy);
        auditService.logStateTransition(updated, previousState, newState, performedBy, null);

        LOG.info("Verification completed for onboarding {}: {}", processId, newState);
        return updated;
    }

    @Transactional
    public OnboardingProcess approve(UUID processID, String comments, String approvedBy) {
        LOG.info("Approving onboarding {}",processID);

        OnboardingProcess process = getProcess(processID);

        // Validate transition
        stateValidator.validateTransition(process.getCurrentState(), OnboardingState.APPROVED);
        
        // Update process
        OnboardingState previousState = process.getCurrentState();
        process.setCurrentState(OnboardingState.APPROVED);
        process.setApprovedBy(approvedBy);
        process.setApprovedAt(Instant.now());
        process.setApprovalComments(comments);
        
        OnboardingProcess updated = processRepository.update(process);
        
        // Audit
        auditService.logApproval(updated, comments, approvedBy);
        auditService.logStateTransition(updated, previousState, OnboardingState.APPROVED, approvedBy, null);
        
        LOG.info("Onboarding {} approved by {}", processID, approvedBy);
        return updated;
    }
    
    @Transactional
    public OnboardingProcess reject(UUID processId, String reason, String rejectedBy) {
        LOG.info("Rejecting onboarding {}", processId);
        
        OnboardingProcess process = getProcess(processId);
        
        // Validate transition
        stateValidator.validateTransition(process.getCurrentState(), OnboardingState.REJECTED);
        
        // Update process
        OnboardingState previousState = process.getCurrentState();
        process.setCurrentState(OnboardingState.REJECTED);
        process.setRejectedBy(rejectedBy);
        process.setRejectedAt(Instant.now());
        process.setRejectionReason(reason);
        
        OnboardingProcess updated = processRepository.update(process);
        
        // Audit
        auditService.logRejection(updated, reason, rejectedBy);
        auditService.logStateTransition(updated, previousState, OnboardingState.REJECTED, rejectedBy, null);
        
        LOG.info("Onboarding {} rejected by {}", processId, rejectedBy);
        return updated;
    }
    
    @Transactional
    public OnboardingProcess complete(UUID processId, String completedBy) {
        LOG.info("Completing onboarding {}", processId);
        
        OnboardingProcess process = getProcess(processId);
        
        // Validate transition 
        stateValidator.validateTransition(process.getCurrentState(), OnboardingState.COMPLETED);
        
        // Update state
        OnboardingState previousState = process.getCurrentState();
        process.setCurrentState(OnboardingState.COMPLETED);
        process.setCompletedAt(Instant.now());
        
        OnboardingProcess updated = processRepository.update(process);
        
        // Audit
        auditService.logEvent(
                updated.getId(),
                updated.getSubject().getEmail(),
                AuditEventType.COMPLETED,
                completedBy,
                "Onboarding process completed successfully",
                null
        );
        auditService.logStateTransition(updated, previousState, OnboardingState.COMPLETED, completedBy, null);

        LOG.info("Onboarding {} completed successfully", processId);
        return updated;
    }

    @Transactional
    public OnboardingProcess cancel(UUID processId, String reason, String cancelledBy) {
        LOG.info("Cancelling onboarding {}", processId);

        OnboardingProcess process = getProcess(processId);

        // Validate transition
        stateValidator.validateTransition(process.getCurrentState(), OnboardingState.CANCELLED);

        // Updated process
        OnboardingState previousState = process.getCurrentState();
        process.setCurrentState(OnboardingState.CANCELLED);
        process.setCancelledBy(cancelledBy);
        process.setCancelledAt(Instant.now());
        process.setCancellationReason(reason);

        OnboardingProcess updated = processRepository.update(process);

        // Audit
        auditService.logEvent(
                updated.getId(),
                updated.getSubject().getEmail(),
                AuditEventType.CANCELLED,
                cancelledBy,
                "Onboarding cancelled: " + reason,
                null
        );
        auditService.logStateTransition(updated, previousState, OnboardingState.CANCELLED, cancelledBy, null);

        LOG.info("Onboarding {} cancelled by {}", processId, cancelledBy);
        return updated;
    }

    // Helper methods

    private OnboardingProcess getProcess(UUID processId) {
        return processRepository.findById(processId)
                .orElseThrow(() -> new OnboardingNotFoundException(processId));
    }
}
