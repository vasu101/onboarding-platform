package com.onboarding.platform.api.controller;

import com.onboarding.platform.api.dto.*;
import com.onboarding.platform.api.mapper.OnboardingMapper;
import com.onboarding.platform.core.process.OnboardingProcess;
import com.onboarding.platform.core.process.OnboardingProcessRepository;
import com.onboarding.platform.core.state.OnboardingState;
import com.onboarding.platform.core.subject.OnboardingSubject;
import com.onboarding.platform.workflow.service.OnboardingWorkflowService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller("/api/onboarding")
@ExecuteOn(TaskExecutors.IO)
public class OnboardingController {

    private static final Logger LOG = LoggerFactory.getLogger(OnboardingController.class);
    private static final String DEFAULT_USER = "system";

    private final OnboardingWorkflowService workflowService;
    private final OnboardingProcessRepository processRepository;
    private final OnboardingMapper mapper;

    public OnboardingController(OnboardingWorkflowService workflowService, OnboardingProcessRepository processRepository, OnboardingMapper mapper) {
        this.workflowService = workflowService;
        this.processRepository = processRepository;
        this.mapper = mapper;
    }

    @Post
    public HttpResponse<OnboardingResponse> createOnboarding(@Valid @Body CreateOnboardingRequest request) {
        LOG.info("Creating onboarding for email: {}", request.getEmail());

        OnboardingSubject subject = mapper.toSubjectEntity(request);
        OnboardingProcess process = workflowService.createOnboarding(subject, DEFAULT_USER);

        return HttpResponse.created(mapper.toResponse(process));
    }

    @Get("/{id}")
    public HttpResponse<OnboardingResponse> getOnboarding(@PathVariable UUID id) {
        LOG.info("Fetching onboarding: {}", id);

        return processRepository.findById(id)
                .map(mapper::toResponse)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Get
    public HttpResponse<List<OnboardingResponse>> getAllOnboardings(@Nullable @QueryValue OnboardingState state) {
        LOG.info("Fetching all onboardings, state filter: {}", state);

        List<OnboardingProcess> processes = state != null
                ? processRepository.findByCurrentState(state)
                : processRepository.findAll();

        List<OnboardingResponse> responses = processes.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return HttpResponse.ok(responses);
    }

    @Post("/{id}/submit")
    public HttpResponse<OnboardingResponse> submitOnboarding(@PathVariable UUID id) {
        LOG.info("Submitting onboarding: {}", id);

        OnboardingProcess process = workflowService.submitForReview(id, DEFAULT_USER);
        return HttpResponse.ok(mapper.toResponse(process));
    }

    @Post("/{id}/request-correction")
    public HttpResponse<OnboardingResponse> requestCorrection(@PathVariable UUID id, @Valid @Body CorrectionRequest request) {
        LOG.info("Requesting corrections for onboarding: {}", id);

        OnboardingProcess process = workflowService.requestCorrection(id, request.getComments(), DEFAULT_USER);
        return HttpResponse.ok(mapper.toResponse(process));
    }

    @Post("/{id}/submit-corrections")
    public HttpResponse<OnboardingResponse> submitCorrections(@PathVariable UUID id) {
        LOG.info("Submitting corrections for onboarding: {}", id);

        OnboardingProcess process = workflowService.submitCorrections(id, DEFAULT_USER);
        return HttpResponse.ok(mapper.toResponse(process));
    }

    @Post("/{id}/start-verification")
    public HttpResponse<OnboardingResponse> startVerification(@PathVariable UUID id) {
        LOG.info("Starting verification for onboarding: {}", id);

        OnboardingProcess process = workflowService.startVerification(id, DEFAULT_USER);
        return HttpResponse.ok(mapper.toResponse(process));
    }

    @Post("/{id}/complete-verification")
    public HttpResponse<OnboardingResponse> completeVerification(@PathVariable UUID id, @Valid @Body VerificationRequest request) {
        LOG.info("Completing verification for onboarding: {}, passed: {}", id, request.getPassed());

        OnboardingProcess process = workflowService.completeVerification(id, request.getPassed(), request.getDetails(), DEFAULT_USER);
        return HttpResponse.ok(mapper.toResponse(process));

    }

    @Post("/{id}/approve")
    public HttpResponse<OnboardingResponse> approve(@PathVariable UUID id, @Nullable @Valid @Body ApprovalRequest request) {
        LOG.info("Approving onboarding: {}", id);

        String comments = request != null ? request.getComments() : null;
        OnboardingProcess process = workflowService.approve(id, comments, DEFAULT_USER);
        return HttpResponse.ok(mapper.toResponse(process));
    }

    @Post("/{id}/reject")
    public HttpResponse<OnboardingResponse> reject(@PathVariable UUID id, @Valid @Body RejectionRequest request) {
        LOG.info("Rejecting onboarding: {}", id);

        OnboardingProcess process = workflowService.reject(id, request.getReason(), DEFAULT_USER);
        return HttpResponse.ok(mapper.toResponse(process));
    }

    @Post("/{id}/complete")
    public HttpResponse<OnboardingResponse> complete(@PathVariable UUID id) {
        LOG.info("Completing onboarding: {}", id);

        OnboardingProcess process = workflowService.complete(id, DEFAULT_USER);
        return HttpResponse.ok(mapper.toResponse(process));
    }

    @Post("/{id}/cancel")
    public HttpResponse<OnboardingResponse> cancel(@PathVariable UUID id, @Valid @Body CancellationRequest request) {
        LOG.info("Cancelling onboarding: {}", id);

        OnboardingProcess process = workflowService.cancel(id, request.getReason() ,DEFAULT_USER);
        return HttpResponse.ok(mapper.toResponse(process));
    }

    @Get("/pending-review")
    public HttpResponse<List<OnboardingResponse>> getPendingReview() {
        LOG.info("Fetching onboardings pending review");

        List<OnboardingProcess> processes = processRepository.findPendingReview();
        List<OnboardingResponse> responses = processes.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return HttpResponse.ok(responses);
    }

    @Get("/requiring-action")
    public HttpResponse<List<OnboardingResponse>> getRequiringAction() {
        LOG.info("Fetching onboardings requiring action");

        List<OnboardingProcess> processes = processRepository.findRequiringSubmitterAction();
        List<OnboardingResponse> responses = processes.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return HttpResponse.ok(responses);
    }

}
