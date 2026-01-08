package com.onboarding.platform.core.process;

import com.onboarding.platform.core.state.OnboardingState;
import com.onboarding.platform.core.subject.OnboardingSubject;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for OnboardingProcess entities.
 * Provides queries for workflow management.
 */
@Repository
public interface OnboardingProcessRepository extends JpaRepository<OnboardingProcess, UUID> {

    /**
     * Find process by subject
     */
    Optional<OnboardingProcess> findBySubject(OnboardingSubject subject);

    /**
     * Find process by subject ID
     */
    Optional<OnboardingProcess> findBySubjectId(UUID subjectId);

    /**
     * Find all processes in a specific state
     */
    List<OnboardingProcess> findByCurrentState(OnboardingState state);

    /**
     * Find all processes in multiple states
     */
    List<OnboardingProcess> findByCurrentStateIn(List<OnboardingState> states);

    /**
     * Find processes that need review (submitted or corrected)
     */
    @Query("SELECT p FROM OnboardingProcess p WHERE p.currentState IN ('SUBMITTED', 'CORRECTED')")
    List<OnboardingProcess> findPendingReview();

    /**
     * Find processes pending approval
     */
    List<OnboardingProcess> findByCurrentStateAndApprovedByIsNull(OnboardingState state);

    /**
     * Find processes that require submitter action
     */
    @Query("SELECT p FROM OnboardingProcess p WHERE p.currentState IN ('DRAFT', 'PENDING_CORRECTION', 'VERIFICATION_FAILED')")
    List<OnboardingProcess> findRequiringSubmitterAction();

    /**
     * Find processes submitted within time range
     */
    List<OnboardingProcess> findBySubmittedAtBetween(Instant start, Instant end);

    /**
     * Find completed processes
     */
    List<OnboardingProcess> findByCurrentStateAndCompletedAtIsNotNull(OnboardingState state);

    /**
     * Count processes by state
     */
    long countByCurrentState(OnboardingState state);

    /**
     * Check if subject has active onboarding
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM OnboardingProcess p " +
            "WHERE p.subject.id = :subjectId AND p.currentState NOT IN ('COMPLETED', 'CANCELLED', 'REJECTED')")
    boolean hasActiveOnboarding(UUID subjectId);

    /**
     * Find processes approaching correction limit
     */
    @Query("SELECT p FROM OnboardingProcess p WHERE p.correctionAttempts >= (p.maxCorrectionAttempts - 1) " +
            "AND p.currentState NOT IN ('COMPLETED', 'CANCELLED', 'REJECTED')")
    List<OnboardingProcess> findApproachingCorrectionLimit();
}
