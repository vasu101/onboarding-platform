package com.onboarding.platform.audit.event;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for audit events.
 * Read-only operations for querying audit trail.
 */
@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    /**
     * Find all events for a specific process (ordered by time)
     */
    List<AuditEvent> findByProcessIdOrderByTimestampDesc(UUID processId);

    /**
     * Find events for a process with pagination
     */
    Page<AuditEvent> findByProcessId(UUID processId, Pageable pageable);

    /**
     * Find events by type
     */
    List<AuditEvent> findByEventTypeOrderByTimestampDesc(AuditEventType eventType);

    /**
     * Find events by performer
     */
    List<AuditEvent> findByPerformedByOrderByTimestampDesc(String performedBy);

    /**
     * Find events in time range
     */
    List<AuditEvent> findByTimestampBetweenOrderByTimestampDesc(Instant start, Instant end);

    /**
     * Find state transition events for a process
     */
    @Query("SELECT e FROM AuditEvent e WHERE e.processId = :processId " +
            "AND e.previousState IS NOT NULL AND e.newState IS NOT NULL " +
            "ORDER BY e.timestamp DESC")
    List<AuditEvent> findStateTransitionsByProcessId(UUID processId);

    /**
     * Find security events
     */
    @Query("SELECT e FROM AuditEvent e WHERE e.eventType IN " +
            "('ACCESS_GRANTED', 'ACCESS_DENIED', 'UNAUTHORIZED_ACCESS_ATTEMPT') " +
            "ORDER BY e.timestamp DESC")
    List<AuditEvent> findSecurityEvents();

    /**
     * Find recent events (last N events)
     */
    List<AuditEvent> findTop50ByOrderByTimestampDesc();

    /**
     * Count events by type
     */
    long countByEventType(AuditEventType eventType);

    /**
     * Find events by subject email
     */
    List<AuditEvent> findBySubjectEmailOrderByTimestampDesc(String email);

    /**
     * Get audit trail summary for a process
     */
    @Query("SELECT e.eventType, COUNT(e) FROM AuditEvent e " +
            "WHERE e.processId = :processId GROUP BY e.eventType")
    List<Object[]> getEventSummaryByProcessId(UUID processId);
}
