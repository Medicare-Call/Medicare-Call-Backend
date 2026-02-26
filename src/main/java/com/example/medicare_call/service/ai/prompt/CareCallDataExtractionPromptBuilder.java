package com.example.medicare_call.service.ai.prompt;

import com.example.medicare_call.dto.data_processor.CareCallDataExtractionRequest;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

@Component
public class CareCallDataExtractionPromptBuilder implements PromptBuilder<CareCallDataExtractionRequest> {

    private final String CARE_CALL_DATA_EXTRACTION_TEMPLATE = """
            다음 통화 내용에서 건강 데이터를 추출하여 JSON 형태로 응답해주세요.

            통화 날짜: {callDate}
            통화 언어: 한국어
            통화 내용:
            {transcriptionText}

            다음 정보들을 추출해주세요. 추출할 수 없는 경우 null로 처리해주세요:

            1. 금일의 날짜
            2. 식사 데이터
               - 식사의 종류 (아침/점심/저녁)
               - 식사 여부 (반드시 "섭취함" 또는 "섭취하지 않음"으로 응답)
               - 식사 간단 요약
            3. 수면 데이터
               - 취침 시작 시각 (HH:mm 형식, 예: 22:00)
               - 취침 종료 시각 (HH:mm 형식, 예: 06:00)
               - 총 수면 시간 (예: 8시간)
            4. 심리 상태 데이터
               - 심리 상태 상세 내용 (짧은 문장들로 요약)
               - 심리 상태 요약 (좋음/나쁨)
            5. 혈당 데이터
               - 측정한 시각
               - 식전 여부
               - 혈당 값 (mg/dL)
               - 혈당 상태 (식전/식후 여부를 고려하여 LOW/NORMAL/HIGH 판단)
            6. 복약 데이터
               - 약의 종류
               - 복약 여부 (반드시 "복용함" 또는 "복용하지 않음"으로 응답)
               - 복용 시간 (반드시 "아침" 또는 "점심" 또는 "저녁" 으로 응답)
            7. 건강 징후 데이터
               - 건강 징후 상세 내용 (짧은 문장들로 요약)
               - 건강 상태 요약 (좋음/나쁨)

            [중요] 복약 데이터 추출 시, 약의 종류는 반드시 다음 리스트에 있는 명칭 중 하나를 사용하세요: [{medicationNames}]
            리스트에 없는 약 이름이 언급된 경우, 가장 유사한 이름으로 매핑하거나, 매핑이 불가능하면 언급된 이름을 그대로 사용하세요.

            [중요] 만약 혈당을 여러 번 측정했거나 여러 종류의 약을 복용했다면, 각각을 반드시 별개의 JSON 객체로 분리하여 배열에 담아주세요. 예를 들어, "아침 혈압약, 점심 당뇨약"은 2개의 복약 데이터 객체로 분리해야 합니다. 절대로 하나의 필드에 여러 정보를 합치지 마세요.
            """;

    private final String JSON_STRUCTURE = """
            응답은 반드시 다음 JSON 구조로 해주세요:
            {
              "date": "날짜",
              "mealData": [
                  {
                    "mealType": "아침/점심/저녁",
                    "mealEatenStatus": "섭취함/섭취하지 않음",
                    "mealSummary": "식사 요약"
                  }
              ],
              "sleepData": {
                "sleepStartTime": "취침 시작 시각",
                "sleepEndTime": "취침 종료 시각",
                "totalSleepTime": "총 수면 시간"
              },
              "psychologicalState": ["심리 상태 상세 내용 1", "심리 상태 상세 내용 2"],
              "psychologicalStatus": "좋음/나쁨",
              "bloodSugarData": [
                {
                  "measurementTime": "측정 시각1",
                  "mealTime": "식전/식후",
                  "bloodSugarValue": 숫자값1,
                  "status": "LOW/NORMAL/HIGH"
                },
                {
                  "measurementTime": "측정 시각2",
                  "mealTime": "식전/식후",
                  "bloodSugarValue": 숫자값2,
                  "status": "LOW/NORMAL/HIGH"
                }
              ],
              "medicationData": [
                {
                  "medicationType": "약 종류1",
                  "taken": "복용함/복용하지 않음",
                  "takenTime": "아침/점심/저녁"
                },
                {
                  "medicationType": "약 종류2",
                  "taken": "복용함/복용하지 않음",
                  "takenTime": "아침/점심/저녁"
                }
              ],
              "healthSigns": ["건강 징후 상세 내용 1", "건강 징후 상세 내용 2"],
              "healthStatus": "좋음/나쁨"
            }
            """;

    @Override
    public String buildSystemMessage() {
        return "당신은 의료 통화 내용에서 건강 데이터를 추출하는 전문가입니다. 주어진 통화 내용에서 건강 관련 정보를 정확히 추출하여 JSON 형태로 응답해주세요.";
    }

    @Override
    public String buildPrompt(CareCallDataExtractionRequest request) {
        String medicationNamesStr = (request.getMedicationNames() != null && !request.getMedicationNames().isEmpty())
                ? String.join(", ", request.getMedicationNames())
                : "등록된 약 없음";

        PromptTemplate promptTemplate = new PromptTemplate(CARE_CALL_DATA_EXTRACTION_TEMPLATE);
        promptTemplate.add("callDate", request.getCallDate());
        promptTemplate.add("transcriptionText", request.getTranscriptionText());
        promptTemplate.add("medicationNames", medicationNamesStr);

        return promptTemplate.create().getContents() + JSON_STRUCTURE;
    }
}
