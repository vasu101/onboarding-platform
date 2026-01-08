package com.onboarding.platform.audit.service;

import com.onboarding.platform.audit.event.AuditEvent;
import com.onboarding.platform.audit.event.AuditEventRepository;
import com.onboarding.platform.audit.event.AuditEventType;
import com.onboarding.platform.core.process.OnboardingProcess;
import com.onboarding.platform.core.state.OnboardingState;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for creating and querying audit events.
 * All audit writes go through this service.
 */
@Singleton
public class AuditService {

    private static final Logger LOG = LoggerFactory.getLogger(AuditService.class);

    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditEventRepository auditEventRepository, ObjectMapper objectMapper) {
        this.auditEventRepository = auditEventRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Log a state transition
     */
    public void logStateTransition(
            OnboardingProcess process,
            OnboardingState previousState,
            OnboardingState newState,
            String performedBy,
            String description
    ) {
        AuditEvent event = AuditEvent.builder()
                .processId(process.getId())
                .subjectEmail(process.getSubject().getEmail())
                .eventType(AuditEventType.STATE_CHANGED)
                .previousState(previousState)
                .newState(newState)
                .performedBy(performedBy)
                .description(description != null ? description :
                        String.format("State changed from %s to %s", previousState, newState))
                .build();

        save(event);
    }

    /**
     * Log process submission
     */
    public void logSubmission(OnboardingProcess process, String performedBy) {
        AuditEvent event = AuditEvent.builder()
                .processId(process.getId())
                .subjectEmail(process.getSubject().getEmail())
                .eventType(AuditEventType.SUBMITTED)
                .newState(OnboardingState.SUBMITTED)
                .performedBy(performedBy)
                .description("Onboarding submitted for review")
                .build();

        save(event);
    }

    /**
     * Log correction request
     */
    public void logCorrectionRequest(
            OnboardingProcess process,
            String comments,
            String performedBy
    ) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("correctionAttempt", process.getCorrectionAttempts() + 1);
        metadata.put("maxAttempts", process.getMaxCorrectionAttempts());
        metadata.put("comments", comments);

        AuditEvent event = AuditEvent.builder()
                .processId(process.getId())
                .subjectEmail(process.getSubject().getEmail())
                .eventType(AuditEventType.CORRECTION_REQUESTED)
                .newState(OnboardingState.PENDING_CORRECTION)
                .performedBy(performedBy)
                .description("Corrections requested: " + comments)
                .metadata(toJson(metadata))
                .build();

        save(event);
    }

    /**
     * Log approval
     */
    public void logApproval(OnboardingProcess process, String comments, String performedBy) {
        Map<String, Object> metadata = new HashMap<>();
        if (comments != null) {
            metadata.put("comments", comments);
        }

        AuditEvent event = AuditEvent.builder()
                .processId(process.getId())
                .subjectEmail(process.getSubject().getEmail())
                .eventType(AuditEventType.APPROVED)
                .newState(OnboardingState.APPROVED)
                .performedBy(performedBy)
                .description("Onboarding approved")
                .metadata(toJson(metadata))
                .build();

        save(event);
    }

    /**
     * Log rejection
     */
    public void logRejection(OnboardingProcess process, String reason, String performedBy) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("reason", reason);
        metadata.put("correctionAttempts", process.getCorrectionAttempts());

        AuditEvent event = AuditEvent.builder()
                .processId(process.getId())
                .subjectEmail(process.getSubject().getEmail())
                .eventType(AuditEventType.REJECTED)
                .newState(OnboardingState.REJECTED)
                .performedBy(performedBy)
                .description("Onboarding rejected: " + reason)
                .metadata(toJson(metadata))
                .build();

        save(event);
    }

    /**
     * Log verification result
     */
    public void logVerification(
            OnboardingProcess process,
            boolean passed,
            String details,
            String performedBy
    ) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("passed", passed);
        metadata.put("details", details);

        AuditEvent event = AuditEvent.builder()
                .processId(process.getId())
                .subjectEmail(process.getSubject().getEmail())
                .eventType(passed ? AuditEventType.VERIFICATION_PASSED : AuditEventType.VERIFICATION_FAILED)
                .performedBy(performedBy)
                .description(passed ? "Verification passed" : "Verification failed: " + details)
                .metadata(toJson(metadata))
                .build();

        save(event);
    }

    /**
     * Log generic event with metadata
     */
    public void logEvent(
            UUID processId,
            String subjectEmail,
            AuditEventType eventType,
            String performedBy,
            String description,
            Map<String, Object> metadata
    ) {
        AuditEvent event = AuditEvent.builder()
                .processId(processId)
                .subjectEmail(subjectEmail)
                .eventType(eventType)
                .performedBy(performedBy)
                .description(description)
                .metadata(metadata != null ? toJson(metadata) : null)
                .build();

        save(event);
    }

    /**
     * Get audit trail for a process
     */
    public List<AuditEvent> getAuditTrail(UUID processId) {
        return auditEventRepository.findByProcessIdOrderByTimestampDesc(processId);
    }

    /**
     * Get state transitions for a process
     */
    public List<AuditEvent> getStateTransitions(UUID processId) {
        return auditEventRepository.findStateTransitionsByProcessId(processId);
    }

    // Private helper methods

    private void save(AuditEvent event) {
        try {
            auditEventRepository.save(event);
            LOG.info("Audit event logged: {} for process {}",
                    event.getEventType(), event.getProcessId());
        } catch (Exception e) {
            // Never fail the main operation due to audit failure
            LOG.error("Failed to save audit event: {}", event.getEventType(), e);
        }
    }

    private String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            LOG.warn("Failed to serialize metadata to JSON", e);
            return null;
        }
    }
}