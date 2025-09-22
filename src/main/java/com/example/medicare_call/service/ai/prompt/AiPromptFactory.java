package com.example.medicare_call.service.ai.prompt;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AiPromptFactory {
    public static Prompt createPrompt(String systemContent, String userContent, OpenAiChatOptions chatOptions) {
        SystemMessage systemMessage = new SystemMessage(systemContent);
        UserMessage userMessage = new UserMessage(userContent);
        return new Prompt(
                List.of(systemMessage, userMessage),
                chatOptions
        );
    }

    public static Prompt createPrompt(String systemContent, String userContent, double temperature) {
        return createPrompt(
                systemContent,
                userContent,
                OpenAiChatOptions.builder().temperature(temperature).build()
        );
    }
}
