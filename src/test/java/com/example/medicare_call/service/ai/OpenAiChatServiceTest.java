package com.example.medicare_call.service.ai;

import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class OpenAiChatServiceTest {

    @Mock
    private OpenAiApi openAiApi;

    @Mock
    private OpenAiChatModel chatModel;

    @InjectMocks
    private OpenAiChatService openAiChatService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(openAiChatService, "openAiApi", openAiApi);
        ReflectionTestUtils.setField(openAiChatService, "chatModel", chatModel);
    }

    @Test
    @DisplayName("openAiChat 메서드는 성공적으로 챗 응답을 생성해야 한다")
    void openAiChat_shouldGenerateChatResponseSuccessfully() {
        // Given
        String userInput = "안녕하세요, 잘 지내고 계신가요?";
        String systemMessage = "당신은 유용한 AI 비서입니다.";
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder().temperature(0.7).model("gpt-4.1").build();
        String expectedContent = "잘 지내고 있습니다, 감사합니다!";

        Prompt mockPrompt = new Prompt(
                List.of(new SystemMessage(systemMessage), new UserMessage(userInput)),
                chatOptions
        );

        AssistantMessage assistantMessage = new AssistantMessage(expectedContent);
        ChatResponse mockChatResponse = new ChatResponse(List.of(new Generation(assistantMessage)));

        when(chatModel.call(any(Prompt.class))).thenReturn(mockChatResponse);

        // When
        ChatResponse response = openAiChatService.openAiChat(userInput, systemMessage, chatOptions);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getResults().get(0).getOutput().toString()).contains(expectedContent);
    }

    @Test
    @DisplayName("openAiChat 메서드는 예외 발생 시 CustomException을 던져야 한다")
    void openAiChat_shouldThrowCustomExceptionOnFailure() {
        // Given
        String userInput = "안녕하세요, 잘 지내고 계신가요?";
        String systemMessage = "당신은 유용한 AI 비서입니다.";
        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder().temperature(0.7).model("gpt-4.1").build();

        Prompt mockPrompt = new Prompt(
                List.of(new SystemMessage(systemMessage), new UserMessage(userInput)),
                chatOptions
        );

        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("API call failed"));

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () ->
                openAiChatService.openAiChat(userInput, systemMessage, chatOptions)
        );
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.OPENAI_API_ERROR);
    }
}
