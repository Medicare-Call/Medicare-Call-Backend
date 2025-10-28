package com.example.medicare_call.service.ai.prompt;

import com.example.medicare_call.dto.report.WeeklySummaryDto;
import com.example.medicare_call.service.statistics.WeeklyStatisticsService;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class WeeklySummaryPromptBuilder implements PromptBuilder<WeeklySummaryDto> {

    private final String WEEKLY_SUMMARY_PROMPT_TEMPLATE = """
            다음은 어르신의 한 주간 건강 데이터입니다. 이 데이터를 바탕으로 보호자를 위한 주간 건강 보고서를 작성해주세요.
            결과는 반드시 공백 포함 80자 이상 100자 미만으로, 존댓말로 작성해주세요.

            [주요 건강 데이터]
            - 주간 총 식사 횟수: {mealCount}회 (총 21끼 기준)
            - 식사율: {mealRate}% (목표: 100%)
            - 평균 수면 시간: {averageSleepHours}시간 (권장: 7-8시간)
            - 약 복용 횟수: {medicationTakenCount}회
            - 놓친 약 횟수: {medicationMissedCount}회
            - 긍정적 심리 상태: {positivePsychologicalCount}회
            - 부정적 심리 상태: {negativePsychologicalCount}회
            - 건강 이상 신호: {healthSignals}회
            - 주간 케어콜 미응답 건수: {missedCalls}회
            - 혈당 측정 결과 (정상/고혈당/저혈당 횟수):
              - 식전: {bloodSugarBeforeMeal}
              - 식후: {bloodSugarAfterMeal}

            [분석 및 보고 가이드라인]
            - 값이 null 이면 해당 항목 미응답(e.g. 복약 여부 응답 안함), 값이 false이면 해당 항목 미수행(e.g. 미복약)임을 참고하여 응답을 생성하세요.
            - 식사율이 70% 미만이면 식사를 잘 챙기실 수 있도록 보호자의 관심이 필요함을 언급해주세요.
            - 놓친 약이 있다면 약 복용의 중요성을 보호자에게 상기시켜주세요.
            - 건강 이상 신호가 1회 이상 감지되었다면, 이 점을 가장 중요한 문제로 보고하고 보호자의 주의를 당부해주세요.
            - 미응답 건수가 1회 이상이면, 어르신의 안위를 확인해볼 필요가 있음을 조언해주세요.
            - 부정적 심리 상태가 긍정적 상태보다 많거나 비슷하면 어르신의 정신 건강에 대한 보호자의 관심을 유도해주세요.
            - 데이터를 종합하여 보호자가 가장 주의해야 할 점 1~2가지를 중심으로 보고서를 작성해주세요.
            """;

    @Override
    public String buildSystemMessage() {
        return "당신은 어르신 주간 건강 보고서 전문가입니다. 어르신의 주간 데이터를 분석하여 보호자에게 필요한 핵심 정보를 80자 이상 100자 미만으로 요약 보고해야 합니다.";
    }

    @Override
    public String buildPrompt(WeeklySummaryDto weeklySummaryDto) {
        PromptTemplate promptTemplate = new PromptTemplate(WEEKLY_SUMMARY_PROMPT_TEMPLATE);

        promptTemplate.add("mealCount", weeklySummaryDto.getMealCount());
        promptTemplate.add("mealRate", weeklySummaryDto.getMealRate());
        promptTemplate.add("averageSleepHours", String.format("%.1f", weeklySummaryDto.getAverageSleepHours()));
        promptTemplate.add("medicationTakenCount", weeklySummaryDto.getMedicationTakenCount());
        promptTemplate.add("medicationMissedCount", weeklySummaryDto.getMedicationMissedCount());
        promptTemplate.add("positivePsychologicalCount", weeklySummaryDto.getPositivePsychologicalCount());
        promptTemplate.add("negativePsychologicalCount", weeklySummaryDto.getNegativePsychologicalCount());
        promptTemplate.add("healthSignals", weeklySummaryDto.getHealthSignals());
        promptTemplate.add("missedCalls", weeklySummaryDto.getMissedCalls());
        var bloodSugar = weeklySummaryDto.getBloodSugar();
        promptTemplate.add("bloodSugarBeforeMeal", formatBloodSugarStats(bloodSugar != null ? bloodSugar.beforeMeal() : null));
        promptTemplate.add("bloodSugarAfterMeal", formatBloodSugarStats(bloodSugar != null ? bloodSugar.afterMeal() : null));

        return promptTemplate.create().getContents();
    }

    private String formatBloodSugarStats(WeeklyStatisticsService.WeeklyBloodSugarType bloodSugarType) {
        if (bloodSugarType == null) {
            return "측정 기록 없음";
        }
        return String.format("정상 %d회, 고혈당 %d회, 저혈당 %d회",
                bloodSugarType.normal(),
                bloodSugarType.high(),
                bloodSugarType.low());
    }
}
