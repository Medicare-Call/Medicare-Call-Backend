package com.example.medicare_call.dto.data_processor.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenAiResponse {
    private List<Choice> choices;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        private Message message;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String content;
    }
} 