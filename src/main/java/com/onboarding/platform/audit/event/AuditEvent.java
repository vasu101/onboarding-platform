package com.onboarding.platform.audit.event;

import com.onboarding.platform.core.state.OnboardingState;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.serde.annotation.Serdeable;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Immutable audit log for all onboarding actions.
 * Write-only, never updated or deleted.
 */
@Entity
@Table(name = "audit_events", indexes = {
        @Index(name = "idx_audit_process_id", columnList = "process_id"),
        @Index(name = "idx_audit_event_type", columnList = "event_type"),
        @Index(name = "idx_audit_performed_by", columnList = "performed_by"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp")
})
@Serdeable
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "process_id", nullable = false)
    private UUID processId;

    @Column(name = "subject_email", length = 100)
    private String subjectEmail;

    @Column(name = "event_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AuditEventType eventType;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "previous_state", length = 50)
    @Enumerated(EnumType.STRING)
    private OnboardingState previousState;

    @Column(name = "new_state", length = 50)
    @Enumerated(EnumType.STRING)
    private OnboardingState newState;

    @Column(name = "performed_by", nullable = false, length = 255)
    private String performedBy;

    @DateCreated
    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    public AuditEvent() {
    }

    public UUID getId() {
        return id;
    }

    public UUID getProcessId() {
        return processId;
    }

    public String getSubjectEmail() {
        return subjectEmail;
    }

    public AuditEventType getEventType() {
        return eventType;
    }

    public String getDescription() {
        return description;
    }

    public OnboardingState getPreviousState() {
        return previousState;
    }

    public OnboardingState getNewState() {
        return newState;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getMetadata() {
        return metadata;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AuditEvent event = new AuditEvent();

        public Builder processId(UUID processId) {
            event.processId = processId;
            return this;
        }

        public Builder subjectEmail(String subjectEmail) {
            event.subjectEmail = subjectEmail;
            return this;
        }

        public Builder eventType(AuditEventType eventType) {
            event.eventType = eventType;
            return this;
        }

        public Builder description(String description) {
            event.description = description;
            return this;
        }

        public Builder previousState(OnboardingState previousState) {
            event.previousState = previousState;
            return this;
        }

        public Builder newState(OnboardingState newState) {
            event.newState = newState;
            return this;
        }

        public Builder performedBy(String performedBy) {
            event.performedBy = performedBy;
            return this;
        }

        public Builder metadata(String metadata) {
            event.metadata = metadata;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            event.ipAddress = ipAddress;
            return this;
        }

        public Builder userAgent(String userAgent) {
            event.userAgent = userAgent;
            return this;
        }

        public AuditEvent build() {
            if (event.processId == null || event.eventType == null || event.performedBy == null) {
                throw new IllegalStateException("processId, eventType, and performedBy are required");
            }
            return event;
        }
    }
}
