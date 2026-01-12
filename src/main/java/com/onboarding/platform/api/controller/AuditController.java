package com.onboarding.platform.api.controller;

import com.onboarding.platform.audit.event.AuditEvent;
import com.onboarding.platform.audit.service.AuditService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@Controller("/api/audit")
@ExecuteOn(TaskExecutors.IO)
public class AuditController {

    private static final Logger LOG = LoggerFactory.getLogger(AuditController.class);
    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @Get("/process/{processId}")
    public HttpResponse<List<AuditEvent>> getAuditTrail(@PathVariable UUID processId) {
        LOG.info("Fetching audit trail for process: {}", processId);

        List<AuditEvent> auditTrail = auditService.getAuditTrail(processId);
        return HttpResponse.ok(auditTrail);
    }

    @Get("/process/{processId}/transitions")
    public HttpResponse<List<AuditEvent>> getStateTransitions(@PathVariable UUID processId) {
        LOG.info("Fetching state transitions for process: {}", processId);

        List<AuditEvent> transitions = auditService.getStateTransitions(processId);
        return HttpResponse.ok(transitions);
    }
}
