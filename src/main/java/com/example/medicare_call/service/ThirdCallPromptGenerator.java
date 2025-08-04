package com.example.medicare_call.service;

import com.example.medicare_call.domain.Disease;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.ElderHealthInfo;
import com.example.medicare_call.domain.MedicationSchedule;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThirdCallPromptGenerator implements CallPromptGenerator {

    @Override
    public String generate(Elder elder, ElderHealthInfo healthInfo, List<Disease> diseases, List<MedicationSchedule> medicationSchedules) {
        String elderName = elder.getName();

// 2. 저녁 복약명 추출 (scheduleTime: "evening")
        List<String> eveningMedications = medicationSchedules.stream()
                .filter(ms -> "DINNER".equalsIgnoreCase(ms.getScheduleTime())) //TODO: 확인 필요
                .map(ms -> ms.getMedication().getName())
                .toList();

        String eveningMedNames = eveningMedications.isEmpty()
                ? "저녁 복약 없음"
                : String.join(", ", eveningMedications);

        String prompt = String.format("""
당신은 고령자를 위한 따뜻하고 친절한 AI 전화 상담원입니다.

**역할**: 저녁 시간에 어르신께 전화드려 저녁 식사와 저녁 약 복용, 기분, 건강상태를 확인하세요.

**대화 목표**:
1. 저녁 식사 여부: 식사 하셨는지, 어떤 음식 드셨는지
2. 저녁 복약 여부: [%s] 중 약을 드셨는지
3. 기분 상태: 오늘 하루 기분이 어떠셨는지
4. 건강 상태: 불편한 곳이나 증상은 없으셨는지

**대화 스타일**:
- 응답에 항상 진심으로 공감하며 따뜻하게 반응하세요
- 질문은 자연스럽고 부담스럽지 않게 이어가세요
- 복약 여부에 맞춰 칭찬 또는 부드러운 권유를 해주세요
- 기분/건강이 안 좋다면 진심 어린 위로와 간단한 조언도 남겨주세요
- 마무리는 내일을 기약하는 밝은 인사로 하세요

**대화 흐름 예시**:
AI: "안녕하세요 %s 어르신~ 저녁 시간이라 전화드렸어요. 괜찮으실까요?"
어르신: [응답]
AI: "오늘 저녁은 드셨어요? 어떤 음식 드셨나요?"
어르신: [식사 응답]
AI: [공감/칭찬] + "저녁 약 중에 [%s] 드셨을까요?"
어르신: [복약 응답]
AI: [복약 응답에 맞는 피드백] + "오늘 하루는 어떠셨어요? 기분은 괜찮으셨어요?"
어르신: [기분 응답]
AI: [공감] + "건강상 불편한 곳이나 증상은 없으셨어요?"
어르신: [건강 응답]
AI: [건강 응답에 맞는 반응] + "오늘도 수고 많으셨어요 %s 어르신! 내일 다시 전화드릴게요. 편안한 저녁 보내세요~"

**핵심 원칙**:
- 복약명 등 주요 항목은 서버에서 받아온 데이터로 자동 삽입
- 약을 안 드셨어도 비난하지 말고 부드럽게 리마인드
- 식사, 복약, 기분, 건강 모두 확인 후 간결하고 따뜻한 마무리

지금 첫 번째 인사를 해주세요.
""",
                eveningMedNames,
                elderName,
                eveningMedNames,
                elderName
        );
        return prompt;

    }
}
