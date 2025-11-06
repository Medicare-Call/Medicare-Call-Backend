package com.example.medicare_call.service.ai.prompt;

import com.example.medicare_call.dto.report.HomeSummaryDto;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class HomeSummaryPromptBuilder implements PromptBuilder<HomeSummaryDto> {

    private final String HOME_SUMMARY_PROMPT_TEMPLATE = """
            다음은 어르신의 현재 건강 상태 데이터입니다. 이 데이터를 바탕으로 보호자가 한눈에 파악할 수 있도록 가장 중요한 내용을 중심으로 45자 이내의 요약 보고서를 작성해주세요.

            [핵심 데이터]
            - 식사: {mealSummary}
            - 복약: {medicationSummary}
            - 수면: {sleepSummary}
            - 건강상태: {healthStatus}
            - 심리상태: {mentalStatus}
            - 평균 혈당: {bloodSugarSummary}

            [보고 가이드라인]
            - '기록되지 않음'은 아직 데이터가 입력되지 않은 상태이므로 '안 했음'으로 해석하지 마세요. '식사하지 않음'과 '기록되지 않음'은 완전히 다른 의미입니다.
            - 기록되지 않은 항목이 많으면 '확인이 필요해 보입니다' 정도로만 언급하고, 부정적으로 추정하지 마세요.
            - 명확히 false인 경우(예: '식사하지 않음')만 우려사항으로 언급하세요.
            - 복약 횟수가 목표에 실제로 미달한 경우만 중요하게 다루세요.
            - 긍정적인 내용은 짧게 언급하거나, 부정적인 내용이 없다면 생략해도 좋습니다.
            """;

    @Override
    public String buildSystemMessage() {
        return "당신은 어르신 건강 요약 보고서 전문가입니다. 어르신 데이터를 분석하여 보호자에게 필요한 핵심 정보를 45자 이내로 요약 보고해야 합니다.";
    }

    @Override
    public String buildPrompt(HomeSummaryDto dto) {
        String mealSummary = Stream.of(
                formatMealStatus(dto.getBreakfast(), "아침"),
                formatMealStatus(dto.getLunch(), "점심"),
                formatMealStatus(dto.getDinner(), "저녁")
        ).collect(Collectors.joining(", "));

        String medicationSummary = String.format("오늘 복약 %d/%d",
                dto.getTotalTakenMedication(), dto.getTotalGoalMedication());


        String sleepSummary = (dto.getSleepHours() == null || dto.getSleepMinutes() == null)
                ? "기록 없음" : String.format("최근 수면 시간: %d시간 %d분", dto.getSleepHours(), dto.getSleepMinutes());

        String bloodSugarSummary = dto.getAverageBloodSugar() != null ?
                dto.getAverageBloodSugar() + " mg/dL" : "기록 없음";

        PromptTemplate promptTemplate = new PromptTemplate(HOME_SUMMARY_PROMPT_TEMPLATE);
        promptTemplate.add("mealSummary", mealSummary);
        promptTemplate.add("medicationSummary", medicationSummary);
        promptTemplate.add("sleepSummary", sleepSummary);
        promptTemplate.add("healthStatus", Objects.toString(dto.getHealthStatus(), "기록 없음"));
        promptTemplate.add("mentalStatus", Objects.toString(dto.getMentalStatus(), "기록 없음"));
        promptTemplate.add("bloodSugarSummary", bloodSugarSummary);

        return promptTemplate.create().getContents();
    }

    // meal data 값이 null일 수도 있으므로 추가 처리
    private String formatMealStatus(Boolean status, String mealName) {
        if (status == null) {
            return mealName + " 기록되지 않음";
        }
        return status ? mealName + " 식사 완료" : mealName + " 식사하지 않음";
    }
}
