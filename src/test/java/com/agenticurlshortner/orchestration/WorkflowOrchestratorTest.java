package com.agenticurlshortner.orchestration;

import com.agenticurlshortner.repository.ExecutionContextRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowOrchestratorTest {

    @Mock
    private ExecutionContextRepository repository;
    @Mock
    private RequirementsAgent requirementsAgent;
    @Mock
    private CoderAgent coderAgent;

    private WorkflowOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new WorkflowOrchestrator(repository, requirementsAgent, coderAgent);
        orchestrator.setRetryDelayMs(0);
        lenient().when(repository.save(any(ExecutionContext.class))).thenAnswer(i -> i.getArguments()[0]);
    }

    @Test
    void startNewWorkflow_Success_TransitionsToPendingApproval() {
        String req = "Build rate limiting";
        when(requirementsAgent.analyzeRequirement(req)).thenReturn("Analysis output");

        ExecutionContext result = orchestrator.startNewWorkflow(req);

        assertEquals(WorkflowState.PENDING_HUMAN_APPROVAL, result.getCurrentState());
        assertEquals("Analysis output", result.getAgentMemory());
        assertEquals(0, result.getRetryCount());
    }

    @Test
    void startNewWorkflow_ApiFailure_ExhaustsRetriesAndFailsSafely() {
        String req = "Build rate limiting";
        when(requirementsAgent.analyzeRequirement(req)).thenThrow(new RuntimeException("API 503"));

        ExecutionContext result = orchestrator.startNewWorkflow(req);

        assertEquals(WorkflowState.FAILED, result.getCurrentState());
        assertEquals(3, result.getRetryCount());
        assertTrue(result.getAgentMemory().contains("Workflow failed after 3 attempts"));

        verify(repository, atLeast(3)).save(any(ExecutionContext.class));
    }

    @Test
    void approveWorkflow_Success_TransitionsToReleaseReady() {
        ExecutionContext context = new ExecutionContext();
        context.setCurrentState(WorkflowState.PENDING_HUMAN_APPROVAL);
        context.setOriginalRequirement("Req");
        context.setAgentMemory("Analysis");

        when(repository.findById(1L)).thenReturn(Optional.of(context));
        when(coderAgent.generateCode("Req", "Analysis")).thenReturn("public class Test {}");

        ExecutionContext result = orchestrator.approveWorkflow(1L);

        assertEquals(WorkflowState.RELEASE_READY, result.getCurrentState());
        assertTrue(result.getAgentMemory().contains("public class Test {}"));
    }

    @Test
    void approveWorkflow_InvalidState_ThrowsException() {
        ExecutionContext context = new ExecutionContext();
        context.setCurrentState(WorkflowState.REQUIREMENT_ANALYSIS); // Wrong state

        when(repository.findById(1L)).thenReturn(Optional.of(context));

        assertThrows(IllegalStateException.class, () -> orchestrator.approveWorkflow(1L));
        verifyNoInteractions(coderAgent); // Ensure code generation is never triggered
    }
}