package com.agenticurlshortner.orchestration;

import com.agenticurlshortner.repository.ExecutionContextRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkflowOrchestrator {
    private final ExecutionContextRepository repository;
    private final RequirementsAgent requirementsAgent;
    private final CoderAgent coderAgent;

    @Value("${orchestrator.retry.delay.ms:5000}")
    private long retryDelayMs;

    public WorkflowOrchestrator(ExecutionContextRepository repository,
                                RequirementsAgent requirementsAgent,
                                CoderAgent coderAgent) {
        this.repository = repository;
        this.requirementsAgent = requirementsAgent;
        this.coderAgent = coderAgent;
    }

    @Transactional
    public ExecutionContext startNewWorkflow(String requirement) {
        ExecutionContext context = new ExecutionContext();
        context.setOriginalRequirement(requirement);
        context.setCurrentState(WorkflowState.REQUIREMENT_ANALYSIS);
        context = repository.save(context);

        String analysis = "";
        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            try {
                analysis = requirementsAgent.analyzeRequirement(requirement);
                break;
            } catch (Exception e) {
                attempt++;
                context.setRetryCount(attempt);
                repository.save(context);

                if (attempt >= maxRetries) {
                    context.setCurrentState(WorkflowState.FAILED);
                    context.setAgentMemory("Workflow failed after " + maxRetries + " attempts. Last API error: " + e.getMessage());
                    return repository.save(context);
                }

                try {
                    Thread.sleep(retryDelayMs * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Workflow interrupted during retry delay", ie);
                }
            }
        }

        context.setAgentMemory(analysis);
        context.setCurrentState(WorkflowState.PENDING_HUMAN_APPROVAL);
        return repository.save(context);
    }

    @Transactional
    public ExecutionContext approveWorkflow(Long id) {
        ExecutionContext context = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found"));

        if (context.getCurrentState() != WorkflowState.PENDING_HUMAN_APPROVAL) {
            throw new IllegalStateException("Workflow is not waiting for approval.");
        }

        context.setCurrentState(WorkflowState.IMPLEMENTATION);
        repository.save(context);

        String generatedCode = coderAgent.generateCode(
                context.getOriginalRequirement(),
                context.getAgentMemory()
        );

        context.setAgentMemory(context.getAgentMemory() + "\n\n### Generated Implementation:\n" + generatedCode);
        context.setCurrentState(WorkflowState.RELEASE_READY);

        return repository.save(context);
    }

    void setRetryDelayMs(long retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
    }
}