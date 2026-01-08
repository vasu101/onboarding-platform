package com.onboarding.platform.core.subject;

import com.onboarding.platform.core.type.OnboardingType;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for OnboardingSubject entities
 */
@Repository
public interface OnboardingSubjectRepository extends JpaRepository<OnboardingSubject, UUID> {
    /**
     * Find subject by email
     */
    Optional<OnboardingSubject> findByEmail(String email);

    /**
     * Check if subject exists by email
     */
    boolean existsByEmail(String email);

    /**
     * Find all subjects by type
     */
    List<OnboardingSubject> findByType(OnboardingType type);

    /**
     * Find subjects by business name (for businesses/partners/vendors)
     */
    List<OnboardingSubject> findByBusinessNameContainsIgnoreCase(String businessName);

    /**
     * Find subjects by full name
     */
    List<OnboardingSubject> findByFullNameContainsIgnoreCase(String fullName);

    /**
     * Find subjects created by specific user
     */
    List<OnboardingSubject> findByCreatedBy(String createdBy);

    /**
     * Find subjects by country
     */
    List<OnboardingSubject> findByCountry(String country);
}
