package com.example.medicare_call.service.ai.prompt;

public interface PromptBuilder<T> {
    String buildSystemMessage();
    String buildPrompt(T dto);
}