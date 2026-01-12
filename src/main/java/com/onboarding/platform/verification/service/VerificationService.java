package com.onboarding.platform.verification.service;

import com.onboarding.platform.core.process.OnboardingProcess;
import com.onboarding.platform.core.subject.OnboardingSubject;
import com.onboarding.platform.core.type.OnboardingType;
import com.onboarding.platform.verification.model.VerificationResult;
import com.onboarding.platform.verification.model.VerificationResult.VerificationCheck;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Service for verifying onboarding information.
 */
@Singleton
public class VerificationService {

    private static final Logger LOG = LoggerFactory.getLogger(VerificationService.class);
    private final Random random = new Random();

    /**
     * Perform comprehensive verification checks
     */
    public VerificationResult verify(OnboardingProcess process) {
        LOG.info("Starting verification for process: {}", process.getId());

        OnboardingSubject subject = process.getSubject();
        VerificationResult result = new VerificationResult();

        if (subject.getType() == OnboardingType.INDIVIDUAL) {
            performIndividualVerification(subject, result);
        } else {
            performBusinessVerification(subject, result);
        }

        boolean allPassed = result.getChecks().stream()
                .allMatch(VerificationCheck::isPassed);

        result.setPassed(allPassed);
        result.setSummary(allPassed ?
                "All verification checks passed" :
                "Some verification checks failed");

        LOG.info("Verification completed for process {}: {}",
                process.getId(), allPassed ? "PASSED" : "FAILED");

        return result;
    }

    /**
     * Verify individual person
     */
    private void performIndividualVerification(OnboardingSubject subject, VerificationResult result) {
        // Email verification
        result.addCheck(verifyEmail(subject.getEmail()));

        // Phone verification
        if (subject.getPhoneNumber() != null) {
            result.addCheck(verifyPhone(subject.getPhoneNumber()));
        }

        // Identity verification (mock)
        result.addCheck(verifyIdentity(subject));

        // Address verification
        if (subject.getAddress() != null) {
            result.addCheck(verifyAddress(subject));
        }
    }

    /**
     * Verify business entity
     */
    private void performBusinessVerification(OnboardingSubject subject, VerificationResult result) {
        // Email verification
        result.addCheck(verifyEmail(subject.getEmail()));

        // Phone verification
        if (subject.getPhoneNumber() != null) {
            result.addCheck(verifyPhone(subject.getPhoneNumber()));
        }

        // Business registration verification
        if (subject.getRegistrationNumber() != null) {
            result.addCheck(verifyBusinessRegistration(subject));
        }

        // Tax ID verification
        if (subject.getTaxId() != null) {
            result.addCheck(verifyTaxId(subject));
        }

        // Address verification
        if (subject.getAddress() != null) {
            result.addCheck(verifyAddress(subject));
        }
    }

    // Individual verification methods (mock implementations)

    private VerificationCheck verifyEmail(String email) {
        // Mock: Simple format check
        boolean valid = email != null && email.contains("@") && email.contains(".");

        // Simulate 90% pass rate
        boolean passed = valid && random.nextInt(100) < 90;

        return new VerificationCheck(
                "EMAIL_VERIFICATION",
                passed,
                passed ? "Email format valid and deliverable" : "Email verification failed"
        );
    }

    private VerificationCheck verifyPhone(String phone) {
        // Mock: Simple format check
        boolean valid = phone != null && phone.length() >= 10;

        // Simulate 85% pass rate
        boolean passed = valid && random.nextInt(100) < 85;

        return new VerificationCheck(
                "PHONE_VERIFICATION",
                passed,
                passed ? "Phone number is valid and active" : "Phone number verification failed"
        );
    }

    private VerificationCheck verifyIdentity(OnboardingSubject subject) {
        // Mock: In production, would call identity verification service
        // Simulate 80% pass rate
        boolean passed = random.nextInt(100) < 80;

        return new VerificationCheck(
                "IDENTITY_VERIFICATION",
                passed,
                passed ?
                        "Identity verified successfully" :
                        "Identity verification failed - document mismatch"
        );
    }

    private VerificationCheck verifyAddress(OnboardingSubject subject) {
        // Mock: Check if address has minimum required fields
        boolean hasRequiredFields = subject.getAddress() != null &&
                subject.getCity() != null &&
                subject.getCountry() != null;

        // Simulate 95% pass rate for complete addresses
        boolean passed = hasRequiredFields && random.nextInt(100) < 95;

        return new VerificationCheck(
                "ADDRESS_VERIFICATION",
                passed,
                passed ? "Address verified" : "Address verification failed"
        );
    }

    private VerificationCheck verifyBusinessRegistration(OnboardingSubject subject) {
        // Mock: In production, would verify against business registry
        // Simulate 75% pass rate
        boolean passed = random.nextInt(100) < 75;

        return new VerificationCheck(
                "BUSINESS_REGISTRATION_VERIFICATION",
                passed,
                passed ?
                        "Business registration number verified" :
                        "Business registration not found in registry"
        );
    }

    private VerificationCheck verifyTaxId(OnboardingSubject subject) {
        // Mock: In production, would verify against tax authority
        // Simulate 80% pass rate
        boolean passed = random.nextInt(100) < 80;

        return new VerificationCheck(
                "TAX_ID_VERIFICATION",
                passed,
                passed ? "Tax ID is valid" : "Tax ID verification failed"
        );
    }

    /**
     * Quick email-only verification (for lightweight checks)
     */
    public boolean quickVerifyEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }

    /**
     * Check if enhanced verification is required
     */
    public boolean requiresEnhancedVerification(OnboardingSubject subject) {
        return subject.getType().requiresEnhancedVerification();
    }
}