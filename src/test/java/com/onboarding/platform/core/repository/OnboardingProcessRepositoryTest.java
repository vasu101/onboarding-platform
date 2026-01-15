package com.onboarding.platform.core.repository;

import com.onboarding.platform.core.process.OnboardingProcess;
import com.onboarding.platform.core.process.OnboardingProcessRepository;
import com.onboarding.platform.core.state.OnboardingState;
import com.onboarding.platform.core.subject.OnboardingSubject;
import com.onboarding.platform.core.subject.OnboardingSubjectRepository;
import com.onboarding.platform.core.type.OnboardingType;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Repository tests for OnboardingProcess
 */
@MicronautTest(transactional = false)
public class OnboardingProcessRepositoryTest {

    @Inject
    OnboardingProcessRepository processRepository;

    @Inject
    OnboardingSubjectRepository subjectRepository;

    private OnboardingSubject testSubject;
    @BeforeEach
    void setUp() {
        testSubject = new OnboardingSubject();
        testSubject.setType(OnboardingType.INDIVIDUAL);
        testSubject.setFullName("Test User");
        testSubject.setEmail("test@example.com");
        testSubject.setCreatedBy("test");
        testSubject = subjectRepository.save(testSubject);
    }

    @AfterEach
    void tearDown() {
        processRepository.deleteAll();;
        subjectRepository.deleteAll();
    }

    @Test
    void testSaveAndFindById() {
        OnboardingProcess process = new OnboardingProcess(testSubject);

        OnboardingProcess saved = processRepository.save(process);
        Optional<OnboardingProcess> found = processRepository.findById(process.getId());

        assertTrue(found.isPresent());
        assertEquals(OnboardingState.DRAFT, found.get().getCurrentState());
        assertEquals(testSubject.getId(), found.get().getSubject().getId());
    }

    @Test
    void testFindBySubject() {
        OnboardingProcess process = new OnboardingProcess(testSubject);
        processRepository.save(process);

        Optional<OnboardingProcess> found = processRepository.findBySubject(testSubject);

        assertTrue(found.isPresent());
        assertEquals(testSubject.getId(), found.get().getSubject().getId());
    }

    @Test
    void testFindByCurrentState() {
        OnboardingProcess process = new OnboardingProcess(testSubject);
        process.setCurrentState(OnboardingState.SUBMITTED);
        processRepository.save(process);

        OnboardingSubject subject = createTestSubject("user1@example.com");
        OnboardingProcess process1 = new OnboardingProcess(subject);
        process1.setCurrentState(OnboardingState.SUBMITTED);
        processRepository.save(process1);

        List<OnboardingProcess> submitted = processRepository.findByCurrentState(OnboardingState.SUBMITTED);

        assertTrue(submitted.size() >= 2);
    }

    @Test
    void testFindPendingReview() {
        OnboardingProcess submitted = new OnboardingProcess(testSubject);
        submitted.setCurrentState(OnboardingState.SUBMITTED);
        processRepository.save(submitted);

        OnboardingSubject subject = createTestSubject("user2@example.com");
        OnboardingProcess corrected = new OnboardingProcess(subject);
        corrected.setCurrentState(OnboardingState.CORRECTED);
        processRepository.save(corrected);

        List<OnboardingProcess> pendingReview = processRepository.findPendingReview();

        assertTrue(pendingReview.size() >= 2);
        assertTrue(pendingReview.stream().anyMatch(p -> p.getCurrentState() == OnboardingState.SUBMITTED));
        assertTrue(pendingReview.stream().anyMatch(p -> p.getCurrentState() == OnboardingState.CORRECTED));
    }

    @Test
    void testHasActiveOnboarding() {
        OnboardingProcess process = new OnboardingProcess(testSubject);
        process.setCurrentState(OnboardingState.SUBMITTED);
        processRepository.save(process);

        boolean hasActive = processRepository.hasActiveOnboarding(testSubject.getId());

        assertTrue(hasActive);
    }

    @Test
    void testCountByState() {
        OnboardingProcess process = new OnboardingProcess(testSubject);
        process.setCurrentState(OnboardingState.DRAFT);
        processRepository.save(process);

        long count = processRepository.countByCurrentState(OnboardingState.DRAFT);

        assertTrue(count >= 1);
    }

    @Test
    void testCorrectionAttempts() {
        OnboardingProcess process = new OnboardingProcess(testSubject);
        process.setCorrectionAttempts(2);
        process.setMaxCorrectionAttempts(3);

        OnboardingProcess saved = processRepository.save(process);

        assertEquals(2, saved.getCorrectionAttempts());
        assertTrue(saved.canRequestCorrection());
    }

    // Helper methods

    private OnboardingSubject createTestSubject(String email) {
        OnboardingSubject subject = new OnboardingSubject();
        subject.setType(OnboardingType.INDIVIDUAL);
        subject.setFullName("Test User");
        subject.setEmail(email);
        subject.setCreatedBy("test");
        return subjectRepository.save(subject);
    }
}
