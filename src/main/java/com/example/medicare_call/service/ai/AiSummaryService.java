package com.example.medicare_call.service.ai;

import com.example.medicare_call.dto.report.HomeSummaryDto;
import com.example.medicare_call.dto.statistics.WeeklyStatsAggregate;
import com.example.medicare_call.service.ai.prompt.HomeSummaryPromptBuilder;
import com.example.medicare_call.service.ai.prompt.SymptomSummaryPromptBuilder;
import com.example.medicare_call.service.ai.prompt.WeeklySummaryPromptBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSummaryService {

    private final OpenAiChatService openAiChatService;
    private final HomeSummaryPromptBuilder homeSummaryPromptBuilder;
    private final WeeklySummaryPromptBuilder weeklySummaryPromptBuilder;
    private final SymptomSummaryPromptBuilder symptomSummaryPromptBuilder;

    @Value("${openai.model}")
    private String openaiModel;

    public String getHomeSummary(HomeSummaryDto homeSummaryDto) {
        try {
            log.info("OpenAI API를 통한 홈 화면 데이터 요약 시작");
            return callWithPrompt(
                    homeSummaryPromptBuilder.buildSystemMessage(),
                    homeSummaryPromptBuilder.buildPrompt(homeSummaryDto),
                    0.3,
                    "AI 요약 정보를 불러오는 데 실패했습니다."
            );
        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생", e);
            return "AI 요약 정보를 불러오는 중 오류가 발생했습니다.";
        }
    }

    public String getWeeklyStatsSummary(WeeklyStatsAggregate aggregate) {
        try {
            log.info("OpenAI API를 통한 주간 건강 데이터 요약 시작");
            return callWithPrompt(
                    weeklySummaryPromptBuilder.buildSystemMessage(),
                    weeklySummaryPromptBuilder.buildPrompt(aggregate),
                    0.7,
                    "주간 건강 데이터 요약에 실패했습니다."
            );
        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생", e);
            return "주간 건강 데이터 요약 중 오류가 발생했습니다.";
        }
    }

    public String getSymptomAnalysis(List<String> symptomList) {
        try {
            log.info("OpenAI API를 통한 증상 분석 코멘트 생성 시작");
            String prompt = symptomSummaryPromptBuilder.buildPrompt(symptomList);
            if (prompt == null) {
                return null;
            }
            return callWithPrompt(
                    symptomSummaryPromptBuilder.buildSystemMessage(),
                    prompt,
                    0.5,
                    "증상 분석 코멘트를 생성하지 못했습니다."
            );
        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생", e);
            return "증상 분석 코멘트 생성 중 오류가 발생했습니다.";
        }
    }

    private String callWithPrompt(String systemMessage, String userPrompt, double temperature, String errorMessage) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(openaiModel)
                .temperature(temperature)
                .build();

        ChatResponse response = openAiChatService.openAiChat(userPrompt, systemMessage, options);

        if (response != null && response.getResult() != null) {
            String content = response.getResult().getOutput().getText();
            log.info("OpenAI 응답: {}", content);
            return content;
        } else {
            log.error("OpenAI API 응답이 비어있습니다");
            return errorMessage;
        }
    }
}
