package com.agenticurlshortner.orchestration;

public enum WorkflowState {
    REQUIREMENT_ANALYSIS,
    ARCHITECTURE_DESIGN,
    IMPLEMENTATION,
    VALIDATION_GUARDRAILS,
    PENDING_HUMAN_APPROVAL,
    RELEASE_READY,
    FAILED,
    ROLLED_BACK
}