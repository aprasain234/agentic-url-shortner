package com.agenticurlshortner.orchestration;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ExecutionContext {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 4096)
    private String originalRequirement;

    @Enumerated(EnumType.STRING)
    private WorkflowState currentState = WorkflowState.REQUIREMENT_ANALYSIS;

    @Lob
    private String agentMemory;

    private int retryCount = 0;

    private LocalDateTime startedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOriginalRequirement() { return originalRequirement; }
    public void setOriginalRequirement(String originalRequirement) { this.originalRequirement = originalRequirement; }
    public WorkflowState getCurrentState() { return currentState; }
    public void setCurrentState(WorkflowState currentState) { this.currentState = currentState; }
    public String getAgentMemory() { return agentMemory; }
    public void setAgentMemory(String agentMemory) { this.agentMemory = agentMemory; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
}