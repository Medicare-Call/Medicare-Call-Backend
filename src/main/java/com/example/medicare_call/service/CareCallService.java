package com.example.medicare_call.service;

import com.example.medicare_call.domain.Disease;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.ElderHealthInfo;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.global.enums.CallType;
import com.example.medicare_call.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class CareCallService {

    @Value("${care-call.url}")
    private String callUrl;

    private final RestTemplate restTemplate;
    private final ElderRepository elderRepository;
    private final ElderHealthInfoRepository healthInfoRepository;
    private final ElderDiseaseRepository elderDiseaseRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;

    private final CallPromptGeneratorFactory callPromptGeneratorFactory;

    public void sendCall(Integer elderId, CallType callType) {
        System.out.println("Call URL: " + callUrl);
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 어르신"));

        ElderHealthInfo healthInfo = healthInfoRepository.findByElderId(elderId);
        List<Disease> diseases = elderDiseaseRepository.findDiseasesByElder(elder);
        List<MedicationSchedule> medicationSchedules = medicationScheduleRepository.findByElderId(elderId);

        // CallPromptGenerator를 CallType으로부터 선택
        CallPromptGenerator promptGenerator = callPromptGeneratorFactory.getGenerator(callType);

        // 선택된 생성기로 프롬프트 생성
        String prompt = promptGenerator.generate(elder, healthInfo, diseases, medicationSchedules);

        sendPrompt(elder.getId(), elder.getPhone(), prompt);
    }

    private void sendPrompt(Integer elderId, String phoneNumber, String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String normalizedPhoneNumber = normalizeKoreanPhoneNumber(phoneNumber);

            Map<String, Object> body = new HashMap<>();
            body.put("elderId", elderId);
            body.put("phoneNumber", normalizedPhoneNumber);
            body.put("prompt", prompt);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(callUrl, request, String.class);

            System.out.println("호출 성공: " + response.getBody());
        } catch (Exception e) {
            System.err.println("호출 실패: " + e.getMessage());
        }
    }

    private String normalizeKoreanPhoneNumber(String phoneNumber) {
        // 010으로 시작하는 경우 +82로 변환
        if (phoneNumber.startsWith("010")) {
            return "+82" + phoneNumber.substring(1);  // "01012345678" → "+821012345678"
        }

        // 이미 +82로 시작하면 그대로 반환
        if (phoneNumber.startsWith("+82")) {
            return phoneNumber;
        }

        log.warn("전화번호 형식이 예상과 다릅니다. 입력값: {}", phoneNumber);
        return phoneNumber;
    }
}
