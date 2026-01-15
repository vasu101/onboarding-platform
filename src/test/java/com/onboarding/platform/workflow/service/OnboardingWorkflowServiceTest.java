package com.onboarding.platform.workflow.service;

import com.onboarding.platform.audit.service.AuditService;
import com.onboarding.platform.core.exception.InvalidStateTransitionException;
import com.onboarding.platform.core.exception.MaxCorrectionAttemptsExceededException;
import com.onboarding.platform.core.exception.OnboardingNotFoundException;
import com.onboarding.platform.core.process.OnboardingProcess;
import com.onboarding.platform.core.process.OnboardingProcessRepository;
import com.onboarding.platform.core.state.OnboardingState;
import com.onboarding.platform.core.subject.OnboardingSubject;
import com.onboarding.platform.core.subject.OnboardingSubjectRepository;
import com.onboarding.platform.core.type.OnboardingType;
import com.onboarding.platform.verification.service.VerificationService;
import com.onboarding.platform.workflow.service.OnboardingWorkflowService;
import com.onboarding.platform.workflow.validation.StateTransitionValidator;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OnboardingWorkFlowService
 */
@MicronautTest
public class OnboardingWorkflowServiceTest {

    private OnboardingWorkflowService workflowService;
    private OnboardingProcessRepository processRepository;
    private OnboardingSubjectRepository subjectRepository;
    private StateTransitionValidator stateValidator;
    private AuditService auditService;
    private VerificationService verificationService;

    @BeforeEach
    void setUp() {
        processRepository = mock(OnboardingProcessRepository.class);
        subjectRepository = mock(OnboardingSubjectRepository.class);
        stateValidator = new StateTransitionValidator();
        auditService = mock(AuditService.class);
        verificationService = mock(VerificationService.class);

        workflowService = new OnboardingWorkflowService(
                processRepository,
                subjectRepository,
                stateValidator,
                auditService,
                verificationService
        );
    }

    @Test
    void testCreateOnboarding() {
        OnboardingSubject subject = createTestSubject();
        when(subjectRepository.existsByEmail(any())).thenReturn(false);
        when(subjectRepository.save(any())).thenReturn(subject);
        when(processRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        OnboardingProcess result = workflowService.createOnboarding(subject, "testUser");

        assertNotNull(result);
        assertEquals(OnboardingState.DRAFT, result.getCurrentState());
        verify(subjectRepository).save(subject);
        verify(processRepository).save(any(OnboardingProcess.class));
        verify(auditService).logEvent(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testSubmitForReview() {
        UUID processId = UUID.randomUUID();
        OnboardingProcess process = createTestProcess();
        when(processRepository.findById(processId)).thenReturn(Optional.of(process));
        when(processRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OnboardingProcess result = workflowService.submitForReview(processId, "testUser");

        assertEquals(OnboardingState.SUBMITTED, result.getCurrentState());
        assertNotNull(result.getSubmittedAt());
        verify(processRepository).update(any());
        verify(auditService).logSubmission(any(), any());
    }

    @Test
    void testSubmitForReview_InvalidState() {
        UUID processId = UUID.randomUUID();
        OnboardingProcess process = createTestProcess();
        process.setCurrentState(OnboardingState.COMPLETED);
        when(processRepository.findById(processId)).thenReturn(Optional.of(process));

        assertThrows(InvalidStateTransitionException.class,
                () -> workflowService.submitForReview(processId, "testUser"));
    }

    @Test
    void testRequestCorrection() {
        UUID processId = UUID.randomUUID();
        OnboardingProcess process = createTestProcess();
        process.setCurrentState(OnboardingState.SUBMITTED);
        process.setCorrectionAttempts(0);
        process.setMaxCorrectionAttempts(3);

        when(processRepository.findById(processId)).thenReturn(Optional.of(process));
        when(processRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OnboardingProcess result = workflowService.requestCorrection(processId, "Fix errors", "reviewer");

        assertEquals(OnboardingState.PENDING_CORRECTION, result.getCurrentState());
        assertEquals(1, result.getCorrectionAttempts());
        assertEquals("Fix errors", result.getCorrectionComments());
        verify(auditService).logCorrectionRequest(any(), any(), any());
    }

    @Test
    void testRequestCorrection_MaxAttemptsExceeded() {
        UUID processId = UUID.randomUUID();
        OnboardingProcess process = createTestProcess();
        process.setCurrentState(OnboardingState.SUBMITTED);
        process.setCorrectionAttempts(3);
        process.setMaxCorrectionAttempts(3);

        when(processRepository.findById(processId)).thenReturn(Optional.of(process));

        assertThrows(MaxCorrectionAttemptsExceededException.class,
                () -> workflowService.requestCorrection(processId, "Fix errors", "reviewer"));
    }

    @Test
    void testApprove() {
        UUID processId = UUID.randomUUID();
        OnboardingProcess process = createTestProcess();
        process.setCurrentState(OnboardingState.PENDING_APPROVAL);

        when(processRepository.findById(processId)).thenReturn(Optional.of(process));
        when(processRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OnboardingProcess result = workflowService.approve(processId, "Looks good", "approver");

        assertEquals(OnboardingState.APPROVED, result.getCurrentState());
        assertEquals("approver", result.getApprovedBy());
        assertNotNull(result.getApprovedAt());
        assertEquals("Looks good", result.getApprovalComments());
        verify(auditService).logApproval(any(), any(), any());
    }

    @Test
    void testReject() {
        UUID processId = UUID.randomUUID();
        OnboardingProcess process = createTestProcess();
        process.setCurrentState(OnboardingState.PENDING_APPROVAL);

        when(processRepository.findById(processId)).thenReturn(Optional.of(process));
        when(processRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OnboardingProcess result = workflowService.reject(processId, "Invalid data", "approver");
        assertEquals(OnboardingState.REJECTED, result.getCurrentState());
        assertEquals("approver", result.getRejectedBy());
        assertNotNull(result.getRejectedAt());
        assertEquals("Invalid data", result.getRejectionReason());
        verify(auditService).logRejection(any(), any(), any());
    }

    @Test
    void testComplete() {
        UUID processId = UUID.randomUUID();
        OnboardingProcess process = createTestProcess();
        process.setCurrentState(OnboardingState.APPROVED);

        when(processRepository.findById(processId)).thenReturn(Optional.of(process));
        when(processRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OnboardingProcess result = workflowService.complete(processId, "admin");

        assertEquals(OnboardingState.COMPLETED, result.getCurrentState());
        assertNotNull(result.getCompletedAt());
        verify(auditService, times(1)).logEvent(any(), any(), any(), any(), any(), any());
    }

    @Test
    void testCancel() {
        UUID processId = UUID.randomUUID();
        OnboardingProcess process = createTestProcess();
        process.setCurrentState(OnboardingState.DRAFT);

        when(processRepository.findById(processId)).thenReturn(Optional.of(process));
        when(processRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OnboardingProcess result = workflowService.cancel(processId, "User cancelled", "user");

        assertEquals(OnboardingState.CANCELLED, result.getCurrentState());
        assertEquals("user", result.getCancelledBy());
        assertEquals("User cancelled", result.getCancellationReason());
        assertNotNull(result.getCancelledAt());
    }

    @Test
    void testProcessNotFound() {
        // Given
        UUID processId = UUID.randomUUID();
        when(processRepository.findById(processId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(OnboardingNotFoundException.class,
                () -> workflowService.submitForReview(processId, "user"));
    }


    // Helper methods

    private OnboardingSubject createTestSubject() {
        OnboardingSubject subject = new OnboardingSubject();
        subject.setId(UUID.randomUUID());
        subject.setType(OnboardingType.INDIVIDUAL);
        subject.setFullName("Test User");
        subject.setEmail("test@example.com");
        return subject;
    }

    private OnboardingProcess createTestProcess() {
        OnboardingProcess process = new OnboardingProcess(createTestSubject());
        process.setId(UUID.randomUUID());
        return process;
    }
}
