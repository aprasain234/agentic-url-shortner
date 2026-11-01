package com.agenticurlshortner.orchestration;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AiConfig {

    @Bean
    public ChatLanguageModel chatLanguageModel(
            @Value("${OPENAI_API_KEY}") String apiKey) {

        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-4o-mini")
                .temperature(0.0)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    @Bean
    public RequirementsAgent requirementsAgent(ChatLanguageModel chatLanguageModel) {
        return AiServices.builder(RequirementsAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }

    @Bean
    public CoderAgent coderAgent(ChatLanguageModel chatLanguageModel) {
        return AiServices.builder(CoderAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }
}