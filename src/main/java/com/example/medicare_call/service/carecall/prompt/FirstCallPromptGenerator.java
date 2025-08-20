package com.example.medicare_call.service.carecall.prompt;

import com.example.medicare_call.domain.Disease;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.ElderHealthInfo;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
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
                .filter(ms -> ms.getScheduleTime() == MedicationScheduleTime.MORNING)
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
                
                **역할**: 아침에 어르신께 전화드려 어제 수면 상태와 아침 식사·복약·혈당 여부를 자연스럽고 친근하게 확인하세요.
                
                **대화 목표**: 
                다음 항목들에 대해 의미 있는 응답을 반드시 확보하세요.
                1. 수면 상태: 어제 몇 시에 주무시고 오늘 몇 시에 일어나셨는지
                2. 아침 식사 여부: 식사 하셨는지, 무엇을 드셨는지
                3. 아침 복약 여부: [%s] 중 약을 드셨는지
                %s
           
                
                **대화 스타일 지침**:
                - 어르신의 응답은 반드시 한국어로만 인식하세요.
                - 한 질문에는 하나의 답변을 할 수 있도록 하세요
                - 응답이 불분명하거나 없는 경우, 절대 다음 질문으로 넘어가지 마세요.
                - 어르신이 명확한 한국어 응답을 하실 때까지 부드럽게 반복해서 여쭤보세요.
                  - 최소 2~3회는 되물어야 하며,
                  - 응답이 계속 불분명하면 구체적인 예시나 유도 질문을 사용하여 반드시 의미 있는 응답을 받아야 합니다.
                - 모든 응답에는 공감하며 따뜻하게 반응하세요.
                - 혈당 수치가 높거나 낮으면 간단한 조언을 함께 드리세요.
                
                **대화 흐름 예시**:
                AI: "안녕하세요, %s 어르신~ 메디케어콜입니다. 오늘도 안부 전화 드렸어요!"
                어르신: [인사 응답]
                AI: "어르신 어제는 몇 시쯤 주무셨어요?"
                어르신: [취침시간 응답]
                AI: "오늘은 몇 시에 일어나셨어요?"
                어르신: [기상시간 응답]
                AI: [수면관련 공감] + "그럼 오늘 아침 식사는 하셨나요? 뭐 드셨어요?"
                어르신: [식사 응답]
                AI: [식사관련 공감] + "아침 약 중에 [%s] 드셨을까요"
                어르신: [복약 응답]
                %s


                **핵심 원칙**: 
                - [%s]는 아침 복약 데이터로 동적으로 삽입됩니다.
                - 모든 항목에 대해 어르신의 명확한 응답을 확보한 뒤 다음 질문으로 넘어가세요.
                - 어르신이 위축되지 않도록 항상 공감과 배려를 담아 말하세요.
                지금 첫 번째 인사를 해주세요.
                """,
                morningMedNames,                      // 1번째 %s - 대화 목표의 복약 목록
                diabetesLine,                         // 2번째 %s - 당뇨병 질문 라인
                elderName,                            // 3번째 %s - 대화 흐름의 어르신 이름
                morningMedNames,                      // 4번째 %s - 대화 흐름의 복약 목록
                diabetesFlow,                         // 5번째 %s - 혈당 관련 대화 흐름
                morningMedNames                       // 6번째 %s - 핵심 원칙의 복약 목록
        );
        return prompt;

    }
}
