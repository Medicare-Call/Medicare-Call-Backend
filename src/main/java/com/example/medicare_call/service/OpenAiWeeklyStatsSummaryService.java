package com.example.medicare_call.service;

import com.example.medicare_call.dto.WeeklySummaryDto;
import com.example.medicare_call.dto.OpenAiRequest;
import com.example.medicare_call.dto.OpenAiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiWeeklyStatsSummaryService {

    private final RestTemplate restTemplate;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.api.url}")
    private String openaiApiUrl;

    @Value("${openai.model}")
    private String openaiModel;

    public String getWeeklyStatsSummary(WeeklySummaryDto weeklySummaryDto) {
        try {
            log.info("OpenAI API를 통한 주간 건강 데이터 요약 시작");

            String prompt = buildPrompt(weeklySummaryDto);

            OpenAiRequest openAiRequest = OpenAiRequest.builder()
                    .model(openaiModel)
                    .messages(List.of(
                            OpenAiRequest.Message.builder()
                                    .role("system")
                                    .content("당신은 주간 건강 데이터를 분석하여 보호자를 위한 주간 건강 보고서를 작성하는 전문가입니다. " +
                                            "제공된 데이터를 기반으로 어르신의 건강 상태를 객관적으로 요약하고, " +
                                            "보호자가 주의 깊게 살펴봐야 할 가장 중요한 사항 1~2가지를 중심으로 조언을 제공해주세요.")
                                    .build(),
                            OpenAiRequest.Message.builder()
                                    .role("user")
                                    .content(prompt)
                                    .build()
                    ))
                    .temperature(0.7) // 다양한 피드백을 위해 약간 높게 설정
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
                return "주간 건강 데이터 요약에 실패했습니다.";
            }

        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생", e);
            return "주간 건강 데이터 요약 중 오류가 발생했습니다.";
        }
    }

    private String buildPrompt(WeeklySummaryDto weeklySummaryDto) {
        return String.format("""
            다음은 어르신의 한 주간 건강 데이터입니다. 이 데이터를 바탕으로 보호자를 위한 주간 건강 보고서를 작성해주세요.
            결과는 반드시 공백 포함 80자 이상 100자 미만으로, 존댓말로 작성해주세요.

            [주요 건강 데이터]
            - 주간 총 식사 횟수: %d회 (총 21끼 기준)
            - 식사율: %d%% (목표: 100%%)
            - 평균 수면 시간: %.1f시간 (권장: 7-8시간)
            - 약 복용 횟수: %d회
            - 놓친 약 횟수: %d회
            - 긍정적 심리 상태: %d회
            - 부정적 심리 상태: %d회
            - 건강 이상 신호: %d회
            - 주간 케어콜 미응답 건수: %d회
            - 혈당 측정 결과 (정상/고혈당/저혈당 횟수):
              - 식전: %s
              - 식후: %s

            [분석 및 보고 가이드라인]
            - 식사율이 70%% 미만이면 식사를 잘 챙기실 수 있도록 보호자의 관심이 필요함을 언급해주세요.
            - 놓친 약이 있다면 약 복용의 중요성을 보호자에게 상기시켜주세요.
            - 건강 이상 신호가 1회 이상 감지되었다면, 이 점을 가장 중요한 문제로 보고하고 보호자의 주의를 당부해주세요.
            - 미응답 건수가 1회 이상이면, 어르신의 안위를 확인해볼 필요가 있음을 조언해주세요.
            - 부정적 심리 상태가 긍정적 상태보다 많거나 비슷하면 어르신의 정신 건강에 대한 보호자의 관심을 유도해주세요.
            - 데이터를 종합하여 보호자가 가장 주의해야 할 점 1~2가지를 중심으로 보고서를 작성해주세요.
            """,
                weeklySummaryDto.getMealCount(),
                weeklySummaryDto.getMealRate(),
                weeklySummaryDto.getAverageSleepHours(),
                weeklySummaryDto.getMedicationTakenCount(),
                weeklySummaryDto.getMedicationMissedCount(),
                weeklySummaryDto.getPositivePsychologicalCount(),
                weeklySummaryDto.getNegativePsychologicalCount(),
                weeklySummaryDto.getHealthSignals(),
                weeklySummaryDto.getMissedCalls(),
                formatBloodSugarStats(weeklySummaryDto.getBloodSugar().getBeforeMeal()),
                formatBloodSugarStats(weeklySummaryDto.getBloodSugar().getAfterMeal())
        );
    }

    private String formatBloodSugarStats(com.example.medicare_call.dto.WeeklyStatsResponse.BloodSugarType bloodSugarType) {
        if (bloodSugarType == null) {
            return "측정 기록 없음";
        }
        return String.format("정상 %d회, 고혈당 %d회, 저혈당 %d회",
                bloodSugarType.getNormal(),
                bloodSugarType.getHigh(),
                bloodSugarType.getLow());
    }
}
