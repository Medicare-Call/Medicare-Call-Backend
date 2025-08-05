package com.example.medicare_call.service;

import com.example.medicare_call.domain.Disease;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.ElderHealthInfo;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.global.enums.CallType;
import com.example.medicare_call.repository.*;
import com.example.medicare_call.util.PhoneNumberUtil;
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

            String normalizedPhoneNumber = PhoneNumberUtil.normalizeKoreanPhoneNumber(phoneNumber);

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
}
