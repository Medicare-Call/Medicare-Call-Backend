package com.example.medicare_call.service.carecall.prompt;

import com.example.medicare_call.domain.Disease;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.ElderHealthInfo;
import com.example.medicare_call.domain.MedicationSchedule;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FirstCallPromptGenerator implements CallPromptGenerator{
    @Override
    public String generate(Elder elder, ElderHealthInfo info,
                           List<Disease> diseases, List<MedicationSchedule> medicationSchedules) {

        String elderName = elder.getName();

        //TODO: "당뇨"로 찾아도 괜찮을지
        boolean hasDiabetes = diseases.stream()
                .anyMatch(d -> d.getName().contains("당뇨"));

        // 아침 복약명 추출 (scheduleTime: "morning")
        List<String> morningMedications = medicationSchedules.stream()
                .filter(ms -> ms.getScheduleTime() != null && ms.getScheduleTime().toUpperCase().contains("MORNING"))
                .map(MedicationSchedule::getName)
                .toList();

        String morningMedNames = morningMedications.isEmpty()
                ? "등록된 아침 복약 없음"
                : String.join(", ", morningMedications);

        String diabetesLine = hasDiabetes
                ? "4. 혈당 여부 (당뇨병이 있는 경우에만): 혈당 재셨는지, 공복/식후 여부, 수치\n" : "";

        String diabetesFlow = hasDiabetes
                ? """
        AI: [공감] + "혈당도 재보셨을까요? 공복에 재셨어요, 식후에 재셨어요? 수치는 몇 나왔는지 기억나세요?"
        어르신: [혈당 응답]
        AI: [수치 반응 및 간단한 조언] + "좋아요 어르신, 오늘 하루도 건강하게 보내시고요, 나중에 또 연락드릴게요~"
        """
                : "AI: \"좋아요 어르신, 오늘 하루도 건강하게 보내시고요, 나중에 또 연락드릴게요~\"\n";

        String prompt = String.format("""
    당신은 고령자를 위한 따뜻하고 친절한 AI 전화 상담원입니다.

    **역할**: 아침에 어르신께 전화드려 어제 수면 상태와 아침 식사·복약%s여부를 자연스럽게 확인하세요.

    **대화 목표**: 다음 항목들을 자연스럽게 확인하세요
    1. 수면 상태: 어제 몇 시에 주무시고 오늘 몇 시에 일어나셨는지
    2. 아침 복약 여부: [%s] 중 약을 드셨는지
    3. 아침 식사 여부: 식사 하셨는지, 무엇을 드셨는지
    %s
    **대화 스타일**:
    - 항상 어르신 응답에 먼저 공감하며 반응하세요
    - 다음 질문은 자연스럽게 연결되도록 하세요
    - 혈당 수치가 높거나 낮으면 간단한 조언을 해주세요
    - 무조건 따뜻하고 친근하게 대화하세요

    **대화 흐름 예시**:
    AI: "안녕하세요, %s 어르신~ 메디케어콜입니다. 오늘도 안부 전화 드렸어요!"
    어르신: [인사 응답]
    AI: "어르신 어제는 몇 시쯤 주무시고, 오늘은 몇 시에 일어나셨어요?"
    어르신: [수면 응답]
    AI: [공감] + "아침 약 중에 [%s] 드셨을까요?"
    어르신: [복약 응답]
    AI: [공감] + "그럼 오늘 아침 식사는 하셨나요? 뭐 드셨어요?"
    어르신: [식사 응답]
    %s
    **핵심 원칙**: 
    - 어르신의 실제 응답을 정확히 반영해서 반응하세요
    - [아침복약1, 2]는 복약데이터로 동적으로 삽입하세요
    - 당뇨병 여부에 따라 혈당 질문 포함 여부를 조절하세요
    - 4가지 항목 확인 후 따뜻하게 마무리 인사하세요

    지금 첫 번째 인사를 해주세요.
    """,
                hasDiabetes ? "·혈당" : "",
                morningMedNames,
                diabetesLine,
                elderName,
                morningMedNames,
                diabetesFlow
        );
        return prompt;

    }
}
