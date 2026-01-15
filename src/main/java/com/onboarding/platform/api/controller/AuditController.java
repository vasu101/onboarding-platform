package com.onboarding.platform.api.controller;

import com.onboarding.platform.audit.event.AuditEvent;
import com.onboarding.platform.audit.service.AuditService;
import com.onboarding.platform.security.annotation.RequiresRole;
import com.onboarding.platform.security.model.UserRole;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@Controller("/api/audit")
@ExecuteOn(TaskExecutors.IO)
@Tag(name = "Audit", description = "Audit trail and logging endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AuditController {

    private static final Logger LOG = LoggerFactory.getLogger(AuditController.class);
    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @Get("/process/{processId}")
    @RequiresRole({UserRole.ADMIN, UserRole.CUSTOMER, UserRole.APPROVER, UserRole.REVIEWER})
    @Operation(
            summary = "Get audit trail",
            description = "Retrieve complete audit trail for an onboarding process"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Audit trail retrieved",
                    content = @Content(schema = @Schema(implementation = AuditEvent.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public HttpResponse<List<AuditEvent>> getAuditTrail(@PathVariable UUID processId) {
        LOG.info("Fetching audit trail for process: {}", processId);

        List<AuditEvent> auditTrail = auditService.getAuditTrail(processId);
        return HttpResponse.ok(auditTrail);
    }

    @Get("/process/{processId}/transitions")
    @RequiresRole({UserRole.ADMIN, UserRole.CUSTOMER, UserRole.APPROVER, UserRole.REVIEWER})
    @Operation(
            summary = "Get state transitions",
            description = "Retrieve only state transition events for an onboarding process"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "State transitions retrieved",
                    content = @Content(schema = @Schema(implementation = AuditEvent.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public HttpResponse<List<AuditEvent>> getStateTransitions(@PathVariable UUID processId) {
        LOG.info("Fetching state transitions for process: {}", processId);

        List<AuditEvent> transitions = auditService.getStateTransitions(processId);
        return HttpResponse.ok(transitions);
    }
}
