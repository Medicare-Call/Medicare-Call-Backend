package com.example.medicare_call.service.data_processor.ai;

import com.example.medicare_call.dto.report.HomeSummaryDto;
import com.example.medicare_call.dto.data_processor.ai.OpenAiRequest;
import com.example.medicare_call.dto.data_processor.ai.OpenAiResponse;
import com.example.medicare_call.dto.report.WeeklyReportResponse;
import com.example.medicare_call.dto.report.WeeklySummaryDto;
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
public class AiSummaryService {

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

            String prompt = buildHomePrompt(homeSummaryDto);

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

            return callOpenAiApi(openAiRequest, "AI 요약 정보를 불러오는 데 실패했습니다.");

        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생", e);
            return "AI 요약 정보를 불러오는 중 오류가 발생했습니다.";
        }
    }

    public String getWeeklyStatsSummary(WeeklySummaryDto weeklySummaryDto) {
        try {
            log.info("OpenAI API를 통한 주간 건강 데이터 요약 시작");

            String prompt = buildWeeklyPrompt(weeklySummaryDto);

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
                    .temperature(0.7)
                    .build();
            
            return callOpenAiApi(openAiRequest, "주간 건강 데이터 요약에 실패했습니다.");

        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생", e);
            return "주간 건강 데이터 요약 중 오류가 발생했습니다.";
        }
    }

    private String callOpenAiApi(OpenAiRequest openAiRequest, String errorMessage) {
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
            return errorMessage;
        }
    }

    // meal data 값이 null일 수도 있으므로 추가 처리
    private String formatMealStatus(Boolean status, String mealName) {
        if (status == null) {
            return mealName + " 기록 존재하지 않음";
        }
        return status ? mealName + " 식사 완료" : mealName + " 식사하지 않음";
    }

    private String buildHomePrompt(HomeSummaryDto dto) {
        String mealSummary = Stream.of(
                formatMealStatus(dto.getBreakfast(), "아침"),
                formatMealStatus(dto.getBreakfast(), "점심"),
                formatMealStatus(dto.getBreakfast(), "저녁")
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
            - 값이 null 이면 해당 항목 미응답(e.g. 복약 여부 응답 안함), 값이 false이면 해당 항목 미수행(e.g. 미복약)임을 참고하여 응답을 생성하세요.
            - 식사, 복약, 수면 중 가장 우려되는 상황을 먼저 언급하세요.
            - 특히, 복약 횟수가 목표에 미달하거나 식사를 거른 경우를 중요하게 다루세요.
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

    private String buildWeeklyPrompt(WeeklySummaryDto weeklySummaryDto) {
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
            - 값이 null 이면 해당 항목 미응답(e.g. 복약 여부 응답 안함), 값이 false이면 해당 항목 미수행(e.g. 미복약)임을 참고하여 응답을 생성하세요.
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

    private String formatBloodSugarStats(WeeklyReportResponse.BloodSugarType bloodSugarType) {
        if (bloodSugarType == null) {
            return "측정 기록 없음";
        }
        return String.format("정상 %d회, 고혈당 %d회, 저혈당 %d회",
                bloodSugarType.getNormal(),
                bloodSugarType.getHigh(),
                bloodSugarType.getLow());
    }

    public String getSymptomAnalysis(List<String> symptomList) {
        try {
            log.info("OpenAI API를 통한 증상 분석 코멘트 생성 시작");

            if (symptomList == null || symptomList.isEmpty()) {
                return null;
            }

            String joinedSymptoms = symptomList.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .collect(Collectors.joining(", "));

            String prompt = String.format("""
                다음은 어르신의 오늘 보고된 증상 목록입니다. 이 증상들을 간단히 묶어 해석하고, 보호자가 바로 취할 수 있는 한 가지 권고를 포함하여 한국어로 공백 포함 100자 내외 한 문장으로 작성해주세요. 의학적 진단 단정은 피하고 존댓말을 사용하세요.

                [증상 목록]
                %s
                """, joinedSymptoms);

            OpenAiRequest openAiRequest = OpenAiRequest.builder()
                    .model(openaiModel)
                    .messages(List.of(
                            OpenAiRequest.Message.builder()
                                    .role("system")
                                    .content("당신은 비의료적 안내 코멘트를 작성하는 전문가입니다. 증상을 종합해 위험 신호 가능성을 부드럽게 알리고, 보호자를 위한 실천적 권고 1가지를 담아 공백 포함 100자 내외 한 문장으로 작성하세요.")
                                    .build(),
                            OpenAiRequest.Message.builder()
                                    .role("user")
                                    .content(prompt)
                                    .build()
                    ))
                    .temperature(0.5)
                    .build();

            return callOpenAiApi(openAiRequest, "증상 분석 코멘트를 생성하지 못했습니다.");

        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생", e);
            return "증상 분석 코멘트 생성 중 오류가 발생했습니다.";
        }
    }
}
