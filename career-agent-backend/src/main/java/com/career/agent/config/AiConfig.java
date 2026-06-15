package com.career.agent.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * AI 配置类 (LangChain4j 版本)
 * 手动创建 ChatLanguageModel Bean（避免 LangChain4j 自动配置的兼容性问题），
 * 并提供 Prompt 模板加载工具。
 */
@Configuration
public class AiConfig {

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.chat-model.model-name}")
    private String modelName;

    @Value("${langchain4j.open-ai.chat-model.temperature:0.3}")
    private Double temperature;

    @Value("${langchain4j.open-ai.chat-model.timeout:120s}")
    private Duration timeout;

    /**
     * 手动创建 ChatLanguageModel Bean。
     * LangChain4j 0.35.0 的 OpenAiAutoConfiguration 在 Spring Boot 3.3.5 下可能
     * 不触发，直接构建 OpenAiChatModel 是最稳定可靠的方式。
     */
    @Bean
    ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(temperature)
                .timeout(timeout)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    /**
     * 从 classpath 加载 Prompt 模板文件。
     */
    public static String loadPromptTemplate(String templateName) {
        try {
            ClassPathResource resource = new ClassPathResource("prompts/" + templateName);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load prompt template: " + templateName, e);
        }
    }
}
