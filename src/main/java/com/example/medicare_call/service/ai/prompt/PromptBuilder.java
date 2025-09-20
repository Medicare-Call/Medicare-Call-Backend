package com.example.medicare_call.service.ai.prompt;

public interface PromptBuilder<T> {
    String buildPrompt(T dto);
}