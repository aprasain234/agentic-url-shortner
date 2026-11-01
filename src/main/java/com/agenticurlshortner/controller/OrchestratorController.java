package com.agenticurlshortner.controller;

import com.agenticurlshortner.orchestration.ExecutionContext;
import com.agenticurlshortner.orchestration.WorkflowOrchestrator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orchestrator")
public class OrchestratorController {

    private final WorkflowOrchestrator orchestrator;

    public OrchestratorController(WorkflowOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/run")
    public ResponseEntity<ExecutionContext> triggerWorkflow(@RequestBody Map<String, String> request) {
        String requirement = request.get("requirement");
        if (requirement == null || requirement.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        ExecutionContext context = orchestrator.startNewWorkflow(requirement);
        return ResponseEntity.ok(context);
    }

    @PostMapping("/run/{id}/approve")
    public ResponseEntity<ExecutionContext> approveWorkflow(@PathVariable Long id) {
        return ResponseEntity.ok(orchestrator.approveWorkflow(id));
    }
}