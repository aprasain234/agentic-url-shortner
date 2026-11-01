package com.agenticurlshortner.orchestration;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface CoderAgent {
    @SystemMessage({
            "You are an expert Java Spring Boot developer.",
            "Based on the original requirement and the architect's analysis, write the exact Java code to implement the changes.",
            "Output only the necessary code snippets and specify which file they belong in."
    })
    @UserMessage("Original Requirement: {{requirement}}\n\nArchitect Analysis:\n{{analysis}}\n\nPlease generate the implementation code.")
    String generateCode(@V("requirement") String requirement, @V("analysis") String analysis);
}