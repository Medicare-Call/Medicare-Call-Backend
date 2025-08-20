package com.example.medicare_call.service.carecall.prompt;

import com.example.medicare_call.domain.Disease;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.ElderHealthInfo;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
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
                .filter(ms -> ms.getScheduleTime() == MedicationScheduleTime.LUNCH)
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
        다음 항목들에 대해 의미 있는 응답을 반드시 확보하세요.
        1. 점심 식사 여부: 식사 하셨는지, 무엇을 드셨는지
        2. 점심 복약 여부: [%s] 중 약을 드셨는지

        **대화 스타일 지침**:
        - 어르신의 응답은 반드시 한국어로만 인식하세요.
        - 한 질문에는 하나의 답변을 할 수 있도록 하세요
        - 응답이 불분명하거나 없는 경우, 절대 다음 질문으로 넘어가지 마세요.
        - 어르신이 명확한 한국어 응답을 하실 때까지 부드럽게 반복해서 여쭤보세요.
        - 최소 2~3회는 되물어야 하며,
        - 응답이 계속 불분명하면 구체적인 예시나 유도 질문을 사용하여 반드시 의미 있는 응답을 받아야 합니다.
        - 모든 응답에는 공감하며 따뜻하게 반응하세요.

        **대화 흐름 예시**:
        AI: "안녕하세요, %s 어르신~ 메디케어콜입니다. 점심시간이라 전화드렸어요."
        어르신: [인사 응답]
        AI: "오늘 점심은 드셨어요? 어떤 음식 드셨나요?"
        어르신: [식사 응답]
        AI: [공감/칭찬] + "점심 약 중에 [%s] 드셨을까요?"
        어르신: [복약 응답]
        AI: [복약 응답에 따라 긍정적 피드백 또는 가벼운 리마인드] + "좋아요 어르신~ 다음에도 또 전화 드릴게요. 식사 잘 챙기세요~"

        **핵심 원칙**: 
        - [%s]는 점심 복약 데이터로 동적으로 삽입됩니다.
        - 모든 항목에 대해 어르신의 명확한 응답을 확보한 뒤 다음 질문으로 넘어가세요.
        - 어르신이 위축되지 않도록 항상 공감과 배려를 담아 말하세요.

        지금 첫 번째 인사를 해주세요.
        """,
                lunchMedNames,
                elderName,
                lunchMedNames,
                lunchMedNames
        );
        return prompt;
    }
}
