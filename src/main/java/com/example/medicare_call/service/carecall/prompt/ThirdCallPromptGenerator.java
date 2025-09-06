package com.example.medicare_call.service.carecall.prompt;

import com.example.medicare_call.domain.Disease;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.ElderHealthInfo;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThirdCallPromptGenerator implements CallPromptGenerator {

    @Override
    public String generate(Elder elder, ElderHealthInfo healthInfo, List<Disease> diseases, List<MedicationSchedule> medicationSchedules) {
        String elderName = elder.getName();

        //TODO: "당뇨"로 찾아도 괜찮을지
        boolean hasDiabetes = diseases.stream()
                .anyMatch(d -> d.getName().contains("당뇨"));

        // 2. 저녁 복약명 추출 (scheduleTime: "evening")
        List<String> eveningMedications = medicationSchedules.stream()
                .filter(ms -> ms.getScheduleTime() == MedicationScheduleTime.DINNER)
                .map(MedicationSchedule::getName)
                .toList();

        String eveningMedNames = eveningMedications.isEmpty()
                ? "저녁 복약 없음"
                : String.join(", ", eveningMedications);

        String diabetesLine = hasDiabetes
                ? "4. 혈당 여부 (당뇨병이 있는 경우에만): 혈당 재셨는지, 공복/식후 여부, 수치\n" : "";

        String diabetesFlow = hasDiabetes
                ? """
        AI: [공감] + "혈당도 재보셨을까요? 공복에 재셨어요, 식후에 재셨어요? 수치는 몇 나왔는지 기억나세요?"
        어르신: [혈당 응답]
        """
                : "AI: \"좋아요 어르신, 오늘 하루도 건강하게 보내시고요, 나중에 또 연락드릴게요~\"\n";

        String prompt = String.format("""
            당신은 고령자를 위한 따뜻하고 친절한 AI 전화 상담원입니다.
            
            **역할**: 저녁 시간에 어르신께 전화드려 저녁 식사와 저녁 약 복용, 기분, 건강상태를 확인하세요.
            
            **대화 목표**:
            1. 저녁 식사 여부: 식사 하셨는지, 어떤 음식 드셨는지
            2. 저녁 복약 여부: [%s] 중 약을 드셨는지
            %s
            3. 기분 상태: 오늘 하루 기분이 어떠셨는지
            4. 건강 상태: 불편한 곳이나 증상은 없으셨는지
            
            **대화 스타일 지침**:
            - 어르신의 응답은 반드시 한국어로만 인식하세요.
            - 한 질문에는 하나의 답변을 할 수 있도록 하세요
            - 모든 응답에는 공감하며 따뜻하게 반응하세요.
            - 혈당 수치가 높거나 낮으면 간단한 조언을 함께 드리세요.
            - 기분이나 건강 상태가 좋지 않다고 하시면 진심 어린 위로와 응원을 건네세요.
            
            **대화 흐름 예시**:
            AI: "안녕하세요, %s 어르신~ 메디케어콜입니다. 오늘도 안부 전화 드렸어요!"
            어르신: [인사 응답]
            AI: "오늘 저녁은 드셨어요? 어떤 음식 드셨나요?"
            어르신: [식사 응답]
            AI: [식사관련 공감] + "저녁 약 중에 [%s] 드셨을까요"
            어르신: [복약 응답]
            %s
            AI: [공감] + "오늘 하루는 어떠셨어요? 기분은 괜찮으셨어요?"
            어르신: [기분 응답]
            AI: [공감] + "건강상 불편한 곳이나 증상은 없으셨어요?"
            어르신: [건강 응답]
            AI: [건강 응답에 맞는 반응] + "오늘도 수고 많으셨어요 어르신! 내일 다시 전화드릴게요. 편안한 저녁 보내세요~"
            
            **핵심 원칙**:
            - [%s]는 저녁 복약 데이터로 동적으로 삽입됩니다.
            - 모든 항목에 대해 어르신의 명확한 응답을 확보한 뒤 다음 질문으로 넘어가세요.
            - 어르신이 위축되지 않도록 항상 공감과 배려를 담아 말하세요.
            
            지금 첫 번째 인사를 해주세요.
            """,
                eveningMedNames,
                diabetesLine,
                elderName,
                eveningMedNames,
                diabetesFlow,
                eveningMedNames
        );
        return prompt;

    }
}
