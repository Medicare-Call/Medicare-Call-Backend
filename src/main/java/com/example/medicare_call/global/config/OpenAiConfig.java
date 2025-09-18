package com.example.medicare_call.global.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAI API 설정
 */
@Configuration
public class OpenAiConfig {
    private static final Logger logger = LoggerFactory.getLogger(OpenAiConfig.class);

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    /**
     * OpenAI API 클라이언트 빈 등록
     */
    @Bean
    public OpenAiApi openAiApi() {
        logger.debug("OpenAI API 클라이언트 초기화");
        return OpenAiApi.builder()
                .apiKey(apiKey)
                .build();
    }

    /**
     * OpenAI ChatModel 빈 등록
     */
    @Bean
    public OpenAiChatModel openAiChatModel(OpenAiApi openAiApi) {
        logger.debug("OpenAI ChatModel 초기화");
        return new OpenAiChatModel(openAiApi);
    }
}
