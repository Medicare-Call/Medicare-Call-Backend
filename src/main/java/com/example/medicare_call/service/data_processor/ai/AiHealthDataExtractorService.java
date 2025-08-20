package com.example.medicare_call.service.data_processor.ai;

import com.example.medicare_call.dto.data_processor.HealthDataExtractionRequest;
import com.example.medicare_call.dto.data_processor.HealthDataExtractionResponse;
import com.example.medicare_call.dto.data_processor.ai.OpenAiRequest;
import com.example.medicare_call.dto.data_processor.ai.OpenAiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
public class AiHealthDataExtractorService {
    
    private static final int JSON_CODE_BLOCK_MARKER_LENGTH = 7; // "```json"
    private static final int CODE_BLOCK_MARKER_LENGTH = 3; // "```"
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.api.url}")
    private String openaiApiUrl;
    
    @Value("${openai.model}")
    private String openaiModel;

    @Autowired
    public AiHealthDataExtractorService(
            @Qualifier("openAiRestTemplate") RestTemplate restTemplate,
            ObjectMapper objectMapper
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }


    public HealthDataExtractionResponse extractHealthData(HealthDataExtractionRequest request) {
        try {
            log.info("OpenAI API를 통한 건강 데이터 추출 시작");
            
            String prompt = buildPrompt(request);
            
            OpenAiRequest openAiRequest = OpenAiRequest.builder()
                    .model(openaiModel)
                    .messages(List.of(
                            // 기본 역할, 정의
                            OpenAiRequest.Message.builder()
                                    .role("system")
                                    .content("당신은 의료 통화 내용에서 건강 데이터를 추출하는 전문가입니다. 주어진 통화 내용에서 건강 관련 정보를 정확히 추출하여 JSON 형태로 응답해주세요.")
                                    .build(),
                            // 전달하는 질문 및 요청
                            OpenAiRequest.Message.builder()
                                    .role("user")
                                    .content(prompt)
                                    .build()
                    ))
                    // temperature: 일관된 그리고 정확한 답변을 주도록 제어하는 파라미터
                    // 0.0 ~ 2.0 -> 클 수록 같은 입력에 대해서도 무작위한 답변, 작을 수록 일관된 답변을 반환
                    .temperature(0.1)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);

            HttpEntity<OpenAiRequest> entity = new HttpEntity<>(openAiRequest, headers);
            
            OpenAiResponse response = restTemplate.postForObject(openaiApiUrl, entity, OpenAiResponse.class);
            
            if (response != null && !response.getChoices().isEmpty()) {
                String content = response.getChoices().get(0).getMessage().getContent();
                log.info("OpenAI 응답: {}", content);
                
                return parseHealthDataResponse(content);
            } else {
                log.error("OpenAI API 응답이 비어있습니다");
                return createEmptyResponse();
            }
            
        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생", e);
            return createEmptyResponse();
        }
    }

    private String buildPrompt(HealthDataExtractionRequest request) {
        return String.format("""
            다음 통화 내용에서 건강 데이터를 추출하여 JSON 형태로 응답해주세요.
            
            통화 날짜: %s
            통화 언어: 한국어
            통화 내용:
            %s
            
            다음 정보들을 추출해주세요. 추출할 수 없는 경우 null로 처리해주세요:
            
            1. 금일의 날짜
            2. 식사 데이터
               - 식사의 종류 (아침/점심/저녁)
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
            
            [중요] 만약 혈당을 여러 번 측정했거나 여러 종류의 약을 복용했다면, 각각을 반드시 별개의 JSON 객체로 분리하여 배열에 담아주세요. 예를 들어, "아침 혈압약, 점심 당뇨약"은 2개의 복약 데이터 객체로 분리해야 합니다. 절대로 하나의 필드에 여러 정보를 합치지 마세요.
            
            응답은 반드시 다음 JSON 구조로 해주세요:
            {
              "date": "날짜",
              "mealData": {
                "mealType": "아침/점심/저녁",
                "mealSummary": "식사 요약"
              },
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
                  "mealTime": "식전/식후1",
                  "bloodSugarValue": 숫자값1,
                  "status": "LOW/NORMAL/HIGH"
                },
                {
                  "measurementTime": "측정 시각2",
                  "mealTime": "식전/식후2",
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
            """, 
            request.getCallDate(),
            request.getTranscriptionText()
        );
    }

    private HealthDataExtractionResponse parseHealthDataResponse(String content) {
        try {
            String jsonContent = content;
            if (content.contains("```json")) {
                jsonContent = content.substring(content.indexOf("```json") + JSON_CODE_BLOCK_MARKER_LENGTH, content.lastIndexOf("```"));
            } else if (content.contains("```")) {
                jsonContent = content.substring(content.indexOf("```") + CODE_BLOCK_MARKER_LENGTH, content.lastIndexOf("```"));
            }
            
            jsonContent = jsonContent.trim();
            return objectMapper.readValue(jsonContent, HealthDataExtractionResponse.class);
            
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 오류", e);
            return createEmptyResponse();
        }
    }

    private HealthDataExtractionResponse createEmptyResponse() {
        return HealthDataExtractionResponse.builder()
                .date(null)
                .mealData(null)
                .sleepData(null)
                .psychologicalState(null)
                .psychologicalStatus(null)
                .bloodSugarData(null)
                .medicationData(null)
                .healthSigns(null)
                .healthStatus(null)
                .build();
    }
} 