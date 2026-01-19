package com.example.medicare_call.service.ai.prompt;

import com.example.medicare_call.dto.statistics.WeeklyStatsAggregate;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class WeeklySummaryPromptBuilder implements PromptBuilder<WeeklyStatsAggregate> {

    private final String WEEKLY_SUMMARY_PROMPT_TEMPLATE = """
            다음은 어르신의 한 주간 건강 데이터입니다. 이 데이터를 바탕으로 보호자를 위한 주간 건강 보고서를 작성해주세요.
            결과는 반드시 공백 포함 80자 이상 100자 미만으로, 존댓말로 작성해주세요.

            [주요 건강 데이터]
            - 주간 총 식사 횟수: {totalMealCount}회 (총 {mealGoalCount}끼 기준)
            - 식사율: {mealRate}% (목표: 100%)
            - 평균 수면 시간: {averageSleepHours} (권장: 7-8시간)
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
        return "당신은 주간 건강 데이터를 분석하여 보호자를 위한 주간 건강 보고서를 작성하는 전문가입니다. " +
                "제공된 데이터를 기반으로 어르신의 건강 상태를 객관적으로 요약하고, " +
                "보호자가 주의 깊게 살펴봐야 할 가장 중요한 사항 1~2가지를 중심으로 조언을 제공해주세요.";
    }

    @Override
    public String buildPrompt(WeeklyStatsAggregate aggregate) {
        PromptTemplate promptTemplate = new PromptTemplate(WEEKLY_SUMMARY_PROMPT_TEMPLATE);

        promptTemplate.add("totalMealCount", aggregate.totalMealCount());
        promptTemplate.add("mealGoalCount", aggregate.mealGoalCount());
        promptTemplate.add("mealRate", aggregate.mealRatePercent());
        promptTemplate.add("averageSleepHours", formatAverageSleepMinutes(aggregate.avgSleepMinutes()));
        promptTemplate.add("medicationTakenCount", aggregate.medicationTakenCount());
        promptTemplate.add("medicationMissedCount", aggregate.medicationMissedCount());
        promptTemplate.add("positivePsychologicalCount", aggregate.psychGoodCount());
        promptTemplate.add("negativePsychologicalCount", aggregate.psychBadCount());
        promptTemplate.add("healthSignals", aggregate.healthSignals());
        promptTemplate.add("missedCalls", aggregate.missedCalls());
        promptTemplate.add("bloodSugarBeforeMeal", formatBloodSugarStats(aggregate.beforeMealBloodSugar()));
        promptTemplate.add("bloodSugarAfterMeal", formatBloodSugarStats(aggregate.afterMealBloodSugar()));

        return promptTemplate.create().getContents();
    }

    private String formatAverageSleepMinutes(Integer minutes) {
        if (minutes == null) {
            return "기록 없음";
        }
        return String.format("%.1f시간", minutes / 60.0);
    }

    private String formatBloodSugarStats(WeeklyStatsAggregate.BloodSugarStats bloodSugar) {
        if (bloodSugar == null) {
            return "측정 기록 없음";
        }
        return String.format("정상 %d회, 고혈당 %d회, 저혈당 %d회",
                bloodSugar.normal(),
                bloodSugar.high(),
                bloodSugar.low());
    }
}
