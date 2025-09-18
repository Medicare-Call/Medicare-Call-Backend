package com.example.medicare_call.service.ai;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * OpenAI API를 사용하여 질의응답을 수행하는 서비스
 */
@RequiredArgsConstructor
@Service
public class OpenAiChatService {
    private static final Logger logger = LoggerFactory.getLogger(OpenAiChatService.class);

    private final OpenAiApi openAiApi;
    private final OpenAiChatModel chatModel;

    /**
     * OpenAI 챗 API를 이용하여 응답을 생성합니다.
     *
     * @param userInput 사용자 입력 메시지
     * @param systemMessage 시스템 프롬프트
     * @param model 사용할 LLM 모델명
     * @return 챗 응답 객체, 오류 시 null
     */
    public ChatResponse openAiChat(
            String userInput,
            String systemMessage,
            String model
    ) {
        logger.debug("OpenAI 챗 호출 시작 - 모델: {}", model);
        try {
            // 메시지 구성
            UserMessage userMessage = new UserMessage(userInput);
            SystemMessage systemPrompt = new SystemMessage(systemMessage);

            // 프롬프트 생성
            Prompt prompt = new Prompt(List.of(userMessage, systemPrompt));

            // 챗 모델 생성 및 호출
            ChatResponse response = chatModel.call(prompt);

            return response;
        } catch (Exception e) {
            logger.error("OpenAI 챗 호출 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }
}
