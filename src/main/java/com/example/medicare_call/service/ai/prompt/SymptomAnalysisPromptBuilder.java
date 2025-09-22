package com.example.medicare_call.service.ai.prompt;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class SymptomAnalysisPromptBuilder implements PromptBuilder<List<String>> {

    private final String SYMPTOM_ANALYSIS_PROMPT_TEMPLATE = """
            다음은 어르신의 오늘 보고된 증상 목록입니다. 이 증상들을 간단히 묶어 해석하고, 보호자가 바로 취할 수 있는 한 가지 권고를 포함하여 한국어로 공백 포함 100자 내외 한 문장으로 작성해주세요. 의학적 진단 단정은 피하고 존댓말을 사용하세요.

            [증상 목록]
            {joinedSymptoms}
            """;

    @Override
    public String buildSystemMessage() {
        return "당신은 어르신 증상 분석 전문가입니다. 주어진 증상 목록을 분석하여 보호자에게 간결한 해석과 한 가지 권고를 100자 이내로 제공해야 합니다.";
    }

    @Override
    public String buildPrompt(List<String> symptomList) {
        if (symptomList == null || symptomList.isEmpty()) {
            return null;
        }

        String joinedSymptoms = symptomList.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.joining(", "));

        PromptTemplate promptTemplate = new PromptTemplate(SYMPTOM_ANALYSIS_PROMPT_TEMPLATE);
        promptTemplate.add("joinedSymptoms", joinedSymptoms);

        return promptTemplate.create().getContents();
    }
}
