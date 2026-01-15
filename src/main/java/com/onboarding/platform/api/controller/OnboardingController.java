package com.onboarding.platform.api.controller;

import com.onboarding.platform.api.dto.*;
import com.onboarding.platform.api.mapper.OnboardingMapper;
import com.onboarding.platform.core.process.OnboardingProcess;
import com.onboarding.platform.core.process.OnboardingProcessRepository;
import com.onboarding.platform.core.state.OnboardingState;
import com.onboarding.platform.core.subject.OnboardingSubject;
import com.onboarding.platform.security.annotation.RequiresRole;
import com.onboarding.platform.security.model.UserRole;
import com.onboarding.platform.security.util.CurrentUserUtil;
import com.onboarding.platform.workflow.service.OnboardingWorkflowService;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller("/api/onboarding")
@ExecuteOn(TaskExecutors.IO)
@Tag(name = "Onboarding", description = "Onboarding workflow management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class OnboardingController {

    private static final Logger LOG = LoggerFactory.getLogger(OnboardingController.class);

    private final OnboardingWorkflowService workflowService;
    private final OnboardingProcessRepository processRepository;
    private final OnboardingMapper mapper;

    public OnboardingController(OnboardingWorkflowService workflowService, OnboardingProcessRepository processRepository, OnboardingMapper mapper) {
        this.workflowService = workflowService;
        this.processRepository = processRepository;
        this.mapper = mapper;
    }

    @Post
    @RequiresRole({UserRole.ADMIN, UserRole.CUSTOMER})
    @Operation(
            summary = "Create new onboarding",
            description = "Create a new onboarding process for a subject. Requires CUSTOMER or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Onboarding created successfully",
                    content = @Content(schema = @Schema(implementation = OnboardingResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public HttpResponse<OnboardingResponse> createOnboarding(@Valid @Body CreateOnboardingRequest request) {
        String currentUser = CurrentUserUtil.getCurrentUsername();
        LOG.info("Creating onboarding for email: {}", request.getEmail());

        OnboardingSubject subject = mapper.toSubjectEntity(request);
        OnboardingProcess process = workflowService.createOnboarding(subject, currentUser);

        return HttpResponse.created(mapper.toResponse(process));
    }

    @Get("/{id}")
    @RequiresRole({UserRole.ADMIN, UserRole.CUSTOMER, UserRole.APPROVER, UserRole.REVIEWER})
    @Operation(
            summary = "Get onboarding by ID",
            description = "Retrieve a specific onboarding process by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Onboarding found",
                    content = @Content(schema = @Schema(implementation = OnboardingResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Onboarding not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public HttpResponse<OnboardingResponse> getOnboarding(@PathVariable UUID id) {
        LOG.info("Fetching onboarding: {}", id);

        return processRepository.findById(id)
                .map(mapper::toResponse)
                .map(HttpResponse::ok)
                .orElse(HttpResponse.notFound());
    }

    @Get
    @RequiresRole({UserRole.ADMIN, UserRole.APPROVER, UserRole.REVIEWER})
    @Operation(
            summary = "List all onboardings",
            description = "Retrieve all onboarding processes, optionally filtered by state. Requires REVIEWER, APPROVER, or ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of onboardings",
                    content = @Content(schema = @Schema(implementation = OnboardingResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
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
    @RequiresRole({UserRole.ADMIN, UserRole.CUSTOMER})
    public HttpResponse<OnboardingResponse> submitOnboarding(@PathVariable UUID id) {
        String currentUser = CurrentUserUtil.getCurrentUsername();
        LOG.info("Submitting onboarding: {}", id);

        OnboardingProcess process = workflowService.submitForReview(id, currentUser);
        return HttpResponse.ok(mapper.toResponse(process));
    }

    @Post("/{id}/request-correction")
    @RequiresRole({UserRole.ADMIN, UserRole.REVIEWER, UserRole.APPROVER})
    public HttpResponse<OnboardingResponse> requestCorrection(@PathVariable UUID id, @Valid @Body CorrectionRequest request) {
        String currentUser = CurrentUserUtil.getCurrentUsername();
        LOG.info("Requesting corrections for onboarding: {}", id);

        OnboardingProcess process = workflowService.requestCorrection(id, request.getComments(), currentUser);
        return HttpResponse.ok(mapper.toResponse(process));
    }

    @Post("/{id}/submit-corrections")
    @RequiresRole({UserRole.ADMIN, UserRole.CUSTOMER})
    public HttpResponse<OnboardingResponse> submitCorrections(@PathVariable UUID id) {
        String currentUser = CurrentUserUtil.getCurrentUsername();
        LOG.info("Submitting corrections for onboarding: {}", id);

        OnboardingProcess process = workflowService.submitCorrections(id, currentUser);
        return HttpResponse.ok(mapper.toResponse(process));
    }

    @Post("/{id}/start-verification")
    @RequiresRole({UserRole.ADMIN, UserRole.REVIEWER})
    public HttpResponse<OnboardingResponse> startVerification(@PathVariable UUID id) {
        String currentUser = CurrentUserUtil.getCurrentUsername();
        LOG.info("Starting verification for onboarding: {}", id);

        OnboardingProcess process = workflowService.startVerification(id, currentUser);
        return HttpResponse.ok(mapper.toResponse(process));
    }

    @Post("/{id}/complete-verification")
    @RequiresRole({UserRole.ADMIN, UserRole.REVIEWER})
    public HttpResponse<OnboardingResponse> completeVerification(@PathVariable UUID id, @Valid @Body VerificationRequest request) {
        String currentUser = CurrentUserUtil.getCurrentUsername();
        LOG.info("Completing verification for onboarding: {}, passed: {}", id, request.getPassed());

        OnboardingProcess process = workflowService.completeVerification(id, request.getPassed(), request.getDetails(), currentUser);
        return HttpResponse.ok(mapper.toResponse(process));

    }

    @Post("/{id}/approve")
    @RequiresRole({UserRole.ADMIN, UserRole.APPROVER})
    public HttpResponse<OnboardingResponse> approve(@PathVariable UUID id, @Nullable @Valid @Body ApprovalRequest request) {
        String currentUser = CurrentUserUtil.getCurrentUsername();
        LOG.info("Approving onboarding: {}", id);

        String comments = request != null ? request.getComments() : null;
        OnboardingProcess process = workflowService.approve(id, comments, currentUser);
        return HttpResponse.ok(mapper.toResponse(process));
    }

    @Post("/{id}/reject")
    @RequiresRole({UserRole.ADMIN, UserRole.APPROVER})
    public HttpResponse<OnboardingResponse> reject(@PathVariable UUID id, @Valid @Body RejectionRequest request) {
        String currentUser = CurrentUserUtil.getCurrentUsername();
        LOG.info("Rejecting onboarding: {}", id);

        OnboardingProcess process = workflowService.reject(id, request.getReason(), currentUser);
        return HttpResponse.ok(mapper.toResponse(process));
    }

    @Post("/{id}/complete")
    @RequiresRole({UserRole.ADMIN})
    public HttpResponse<OnboardingResponse> complete(@PathVariable UUID id) {
        String currentUser =CurrentUserUtil.getCurrentUsername();
        LOG.info("Completing onboarding: {}", id);

        OnboardingProcess process = workflowService.complete(id, currentUser);
        return HttpResponse.ok(mapper.toResponse(process));
    }

    @Post("/{id}/cancel")
    @RequiresRole({UserRole.ADMIN, UserRole.CUSTOMER})
    public HttpResponse<OnboardingResponse> cancel(@PathVariable UUID id, @Valid @Body CancellationRequest request) {
        String currentUser = CurrentUserUtil.getCurrentUsername();
        LOG.info("Cancelling onboarding: {}", id);

        OnboardingProcess process = workflowService.cancel(id, request.getReason() ,currentUser);
        return HttpResponse.ok(mapper.toResponse(process));
    }

    @Get("/pending-review")
    @RequiresRole({UserRole.ADMIN, UserRole.REVIEWER, UserRole.APPROVER})
    public HttpResponse<List<OnboardingResponse>> getPendingReview() {
        LOG.info("Fetching onboardings pending review");

        List<OnboardingProcess> processes = processRepository.findPendingReview();
        List<OnboardingResponse> responses = processes.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return HttpResponse.ok(responses);
    }

    @Get("/requiring-action")
    @RequiresRole({UserRole.ADMIN, UserRole.CUSTOMER})
    public HttpResponse<List<OnboardingResponse>> getRequiringAction() {
        LOG.info("Fetching onboardings requiring action");

        List<OnboardingProcess> processes = processRepository.findRequiringSubmitterAction();
        List<OnboardingResponse> responses = processes.stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return HttpResponse.ok(responses);
    }

}
