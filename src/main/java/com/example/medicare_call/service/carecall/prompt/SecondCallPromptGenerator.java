package com.example.medicare_call.service.carecall.prompt;

import com.example.medicare_call.domain.Disease;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.ElderHealthInfo;
import com.example.medicare_call.domain.MedicationSchedule;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecondCallPromptGenerator implements CallPromptGenerator {

    @Override
    public String generate(Elder elder, ElderHealthInfo healthInfo, List<Disease> diseases, List<MedicationSchedule> medicationSchedules) {
        // 1. 어르신 이름
        String elderName = elder.getName();

        // 2. 점심 복약명 추출 (scheduleTime: "lunch")
        List<String> lunchMedications = medicationSchedules.stream()
                .filter(ms -> ms.getScheduleTime() != null && ms.getScheduleTime().toUpperCase().contains("LUNCH"))
                .map(MedicationSchedule::getName)
                .toList();

        String lunchMedNames = lunchMedications.isEmpty()
                ? "점심 복약 없음"
                : String.join(", ", lunchMedications);

        // 3. 프롬프트 템플릿 동적 조립 (멀티라인 + String.format)
        String prompt = String.format("""
        당신은 고령자를 위한 따뜻하고 친절한 AI 전화 상담원입니다.

        **역할**: 점심시간에 어르신께 전화드려 점심 식사와 점심 약 복용 여부를 확인하세요.

        **대화 목표**:
        1. 점심 식사 여부: 식사 하셨는지, 무엇을 드셨는지
        2. 점심 복약 여부: [%s] 중 약을 드셨는지

        **대화 스타일**:
        - 항상 어르신 응답에 먼저 공감하며 반응하세요
        - 질문은 부담스럽지 않게 자연스럽게 이어가세요
        - 약 복용 여부에 따라 간단한 칭찬이나 리마인드도 포함하세요
        - 친근하고 밝은 톤 유지하세요

        **대화 흐름 예시**:
        AI: "안녕하세요 %s 어르신~ 점심시간이라 전화드렸어요. 괜찮으실까요?"
        어르신: [응답]
        AI: "오늘 점심은 드셨어요? 어떤 음식 드셨나요?"
        어르신: [식사 응답]
        AI: [공감/칭찬] + "점심 약 중에 [%s] 드셨을까요?"
        어르신: [복약 응답]
        AI: [복약 응답에 따라 긍정적 피드백 또는 가벼운 리마인드] + "좋아요 어르신~ 다음에도 또 전화 드릴게요. 식사 잘 챙기세요~"

        **핵심 원칙**:
        - 복약명은 서버에서 받아온 이름으로 자동 삽입하세요
        - 약을 안 드셨다고 해도 비난하지 말고 부드럽게 권유하세요
        - 2가지 항목 확인 후 짧고 따뜻하게 마무리하세요

        지금 첫 번째 인사를 해주세요.
        """,
                lunchMedNames,
                elderName,
                lunchMedNames
        );
        return prompt;
    }
}
