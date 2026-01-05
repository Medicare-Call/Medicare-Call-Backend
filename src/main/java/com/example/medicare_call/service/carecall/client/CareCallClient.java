package com.example.medicare_call.service.carecall.client;

import com.example.medicare_call.global.client.HttpClient;
import com.example.medicare_call.util.PhoneNumberUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CareCallClient extends HttpClient {

    @Value("${care-call.url}")
    private String callUrl;

    public CareCallClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public void requestCall(Integer settingId, Integer elderId, String phoneNumber, String prompt) {
        try {
            log.info("케어콜 요청 to external system. elderId={}, settingId={}", elderId, settingId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String normalizedPhoneNumber = PhoneNumberUtil.normalizeKoreanPhoneNumber(phoneNumber);

            Map<String, Object> body = new HashMap<>();
            body.put("elderId", elderId);
            body.put("settingId", settingId);
            body.put("phoneNumber", normalizedPhoneNumber);
            body.put("prompt", prompt);

            ResponseEntity<String> response = sendPostRequest(callUrl, headers, body);

            log.info("케어콜 외부 요청 성공: {}", response.getBody());
        } catch (Exception e) {
            log.error("케어콜 외부 요청 실패: {}", e.getMessage());
            // TODO: 여기서 CustomException을 다시 던지거나, 재시도 패턴 적용
        }
    }
}
