package com.agenticurlshortner.orchestration;

import dev.langchain4j.service.SystemMessage;

public interface RequirementsAgent {

    @SystemMessage({
            "You are a Principal Software Engineer at Charles Schwab.",
            "Analyze the following requirement for a URL shortener system.",
            "1. Identify any ambiguities.",
            "2. Break it down into an actionable engineering task list.",
            "3. Identify architectural impact.",
            "Return your analysis in clear Markdown format."
    })
    String analyzeRequirement(String userRequirement);
}