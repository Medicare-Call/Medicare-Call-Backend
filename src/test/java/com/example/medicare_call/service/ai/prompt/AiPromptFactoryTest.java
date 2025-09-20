package com.example.medicare_call.service.ai.prompt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AiPromptFactoryTest {

    @Test
    @DisplayName("createPrompt에서 Prompt 객체 생성")
    void createPrompt_shouldCreateCorrectPrompt() {
        // Given
        String systemContent = "당신은 AI 비서입니다.";
        String userContent = "안녕 2024, 안녕 2025를 영어로 번역해줘.";
        double temperature = 0.7;

        // When
        Prompt prompt = AiPromptFactory.createPrompt(systemContent, userContent, temperature);

        // Then
        assertThat(prompt).isNotNull();
        String contents = prompt.getContents();
        assertThat(contents).contains(systemContent);
        assertThat(contents).contains(userContent);

        OpenAiChatOptions options = (OpenAiChatOptions) prompt.getOptions();
        assertThat(options).isNotNull();
        assertThat(options.getTemperature()).isEqualTo(temperature);
    }

    @Test
    @DisplayName("createPrompt에서 temperature가 0.0일 때 Prompt 객체 생성")
    void createPrompt_shouldCreateCorrectPrompt_whenTemperatureIsZero() {
        // Given
        String systemContent = "당신은 AI 비서입니다.";
        String userContent = "안녕 2024, 안녕 2025를 영어로 번역해줘.";
        double temperature = 0.0;

        // When
        Prompt prompt = AiPromptFactory.createPrompt(systemContent, userContent, temperature);

        // Then
        assertThat(prompt).isNotNull();
        String contents = prompt.getContents();
        assertThat(contents).contains(systemContent);
        assertThat(contents).contains(userContent);

        OpenAiChatOptions options = (OpenAiChatOptions) prompt.getOptions();
        assertThat(options).isNotNull();
        assertThat(options.getTemperature()).isEqualTo(temperature);
    }
}
