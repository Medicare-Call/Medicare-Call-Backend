package com.example.medicare_call.service;

import com.example.medicare_call.dto.HomeSummaryDto;
import com.example.medicare_call.dto.OpenAiRequest;
import com.example.medicare_call.dto.OpenAiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiHomeSummaryService {

    private final RestTemplate restTemplate;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.api.url}")
    private String openaiApiUrl;

    @Value("${openai.model}")
    private String openaiModel;

    public String getHomeSummary(HomeSummaryDto homeSummaryDto) {
        try {
            log.info("OpenAI API를 통한 홈 화면 데이터 요약 시작");

            String prompt = buildPrompt(homeSummaryDto);

            OpenAiRequest openAiRequest = OpenAiRequest.builder()
                    .model(openaiModel)
                    .messages(List.of(
                            OpenAiRequest.Message.builder()
                                    .role("system")
                                    .content("당신은 홈 화면의 건강 데이터를 분석하여 보호자를 위한 간결한 요약 보고서를 작성하는 전문가입니다. " +
                                            "가장 중요하거나 시급한 건강 이슈 1~2가지를 중심으로, 공백 포함 45자 이내의 짧은 문장을 생성해주세요.")
                                    .build(),
                            OpenAiRequest.Message.builder()
                                    .role("user")
                                    .content(prompt)
                                    .build()
                    ))
                    .temperature(0.3)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);

            HttpEntity<OpenAiRequest> entity = new HttpEntity<>(openAiRequest, headers);

            OpenAiResponse response = restTemplate.postForObject(openaiApiUrl, entity, OpenAiResponse.class);

            if (response != null && !response.getChoices().isEmpty()) {
                String content = response.getChoices().get(0).getMessage().getContent();
                log.info("OpenAI 응답: {}", content);
                return content;
            } else {
                log.error("OpenAI API 응답이 비어있습니다");
                return "AI 요약 정보를 불러오는 데 실패했습니다.";
            }

        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생", e);
            return "AI 요약 정보를 불러오는 중 오류가 발생했습니다.";
        }
    }

    private String buildPrompt(HomeSummaryDto dto) {
        String mealSummary = Stream.of(
                dto.getBreakfast() ? "아침 식사 완료" : "아침 식사 누락",
                dto.getLunch() ? "점심 식사 완료" : "점심 식사 누락",
                dto.getDinner() ? "저녁 식사 완료" : "저녁 식사 누락"
        ).collect(Collectors.joining(", "));

        String medicationSummary = String.format("오늘 복약 %d/%d, 다음 복약: %s",
                dto.getTotalTakenMedication(), dto.getTotalGoalMedication(), dto.getNextMedicationTime());

        String sleepSummary = String.format("최근 수면 시간: %d시간 %d분",
                dto.getSleepHours(), dto.getSleepMinutes());

        String bloodSugarSummary = dto.getAverageBloodSugar() != null ?
                dto.getAverageBloodSugar() + " mg/dL" : "기록 없음";

        return String.format("""
            다음은 어르신의 현재 건강 상태 데이터입니다. 이 데이터를 바탕으로 보호자가 한눈에 파악할 수 있도록 가장 중요한 내용을 중심으로 45자 이내의 요약 보고서를 작성해주세요.
            
            [핵심 데이터]
            - 식사: %s
            - 복약: %s
            - 수면: %s
            - 건강상태: %s
            - 심리상태: %s
            - 평균 혈당: %s
            
            [보고 가이드라인]
            - 식사, 복약, 수면 중 가장 우려되는 상황을 먼저 언급하세요.
            - 특히, 복약 횟수가 목표에 미달하거나 식사를 거른 경우를 중요하게 다루세요.
            - 건강/심리 상태가 '나쁨' 또는 '불안'과 같은 부정적 상태일 경우 이를 반드시 포함시키세요.
            - 긍정적인 내용은 짧게 언급하거나, 부정적인 내용이 없다면 생략해도 좋습니다.
            """,
                mealSummary,
                medicationSummary,
                sleepSummary,
                Objects.toString(dto.getHealthStatus(), "기록 없음"),
                Objects.toString(dto.getMentalStatus(), "기록 없음"),
                bloodSugarSummary
        );
    }
}
