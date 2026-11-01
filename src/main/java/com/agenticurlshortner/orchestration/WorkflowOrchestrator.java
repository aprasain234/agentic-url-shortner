package com.agenticurlshortner.orchestration;

import com.agenticurlshortner.repository.ExecutionContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkflowOrchestrator {
    private final ExecutionContextRepository repository;
    private final RequirementsAgent requirementsAgent;

    public WorkflowOrchestrator(ExecutionContextRepository repository, RequirementsAgent requirementsAgent) {
        this.repository = repository;
        this.requirementsAgent = requirementsAgent;
    }

    @Transactional
    public ExecutionContext startNewWorkflow(String requirement) {
        // 1. Initialize State
        ExecutionContext context = new ExecutionContext();
        context.setOriginalRequirement(requirement);
        context.setCurrentState(WorkflowState.REQUIREMENT_ANALYSIS);
        context = repository.save(context);

        // 2. Execute Agentic Task with Bounded Retries
        String analysis = "";
        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                analysis = requirementsAgent.analyzeRequirement(requirement);
                break; // Success, break out of the retry loop
            } catch (Exception e) {
                attempt++;
                context.setRetryCount(attempt);
                repository.save(context); // Audit observability

                if (attempt >= maxRetries) {
                    // Safe-stop control
                    context.setCurrentState(WorkflowState.FAILED);
                    context.setAgentMemory("Workflow failed after " + maxRetries + " attempts. Last API error: " + e.getMessage());
                    return repository.save(context);
                }

                try {
                    // Exponential backoff: sleep for 5s, then 10s before retrying
                    Thread.sleep(5000L * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Workflow interrupted during retry delay", ie);
                }
            }
        }

        // 3. Update State and enforce Human-In-The-Loop gate
        context.setAgentMemory(analysis);
        context.setCurrentState(WorkflowState.PENDING_HUMAN_APPROVAL);

        return repository.save(context);
    }
}