package com.example.medicare_call.service;

import com.example.medicare_call.domain.Disease;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.ElderHealthInfo;
import com.example.medicare_call.global.enums.CallType;
import com.example.medicare_call.repository.DiseaseRepository;
import com.example.medicare_call.repository.ElderDiseaseRepository;
import com.example.medicare_call.repository.ElderHealthInfoRepository;
import com.example.medicare_call.repository.ElderRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CareCallService {

    @Value("${care-call.url}")
    private String callUrl;

//    private final RestTemplate restTemplate = new RestTemplate();
    private final RestTemplate restTemplate;
    private final ElderRepository elderRepository;
    private final ElderHealthInfoRepository healthInfoRepository;
    private final DiseaseRepository diseaseRepository;
    private final ElderDiseaseRepository elderDiseaseRepository;

    @PostConstruct
    public void checkConfiguration() {
        System.out.println("=== CareCallService 설정 확인 ===");
        System.out.println("Care Call URL: " + callUrl);
        System.out.println("환경변수 CARE_CALL_URL: " + System.getenv("CARE_CALL_URL"));
        System.out.println("===============================");
    }

    public void sendCall(Integer elderId, CallType callType) {
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 어르신"));

        //TODO: null값일 때 처리
        ElderHealthInfo healthInfo = healthInfoRepository.findByElderId(elderId);
        List<Disease> diseases = elderDiseaseRepository.findDiseasesByElder(elder);

        String prompt = generatePrompt(elder, healthInfo, diseases, callType);
        sendPrompt(prompt);
    }

    //TODO: CallType에 따라서 전달하는 1,2,3차 프롬프트 내용 알맞게 수정
    private String generatePrompt(Elder elder, ElderHealthInfo healthInfo, List<Disease> diseases, CallType callType) {
        StringBuilder sb = new StringBuilder();

        sb.append("당신은 ").append(elder.getName()).append(" 어르신을 위한 AI 전화 상담원입니다.\n\n");

        sb.append("어르신 정보\n")
                .append("- 성별: ").append(elder.getGender() == 1 ? "남성" : "여성").append("\n")
                .append("- 생년월일: ").append(elder.getBirthDate()).append("\n");

        sb.append("건강 정보\n")
                .append("- 질환: ").append(diseases.isEmpty() ? "없음" :
                        diseases.stream().map(Disease::getName).collect(Collectors.joining(", ")))
                .append("\n");

        if (healthInfo != null && healthInfo.getNotes() != null) {
            sb.append("- 보호자 메모: ").append(healthInfo.getNotes()).append("\n");
        }

        sb.append("\n지금 인사를 시작해 주세요.");
        return sb.toString();
    }

    private void sendPrompt(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("prompt", prompt);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(callUrl, request, String.class);

            System.out.println("호출 성공: " + response.getBody());
        } catch (Exception e) {
            System.err.println("호출 실패: " + e.getMessage());
        }
    }

    //TODO: 테스트용 함수 삭제
    /*-------------------------테스트용 GET요청 함수, 추후 삭제, 시작-------------------------*/
    public String testGetRequest() {
        try {
            System.out.println("테스트 GET 요청 URL: " + callUrl);

            ResponseEntity<String> response = restTemplate.getForEntity(callUrl, String.class);

            String result = "GET 요청 성공, 상태코드: " + response.getStatusCode() +
                    ", 응답: " + response.getBody();
            System.out.println(result);
            return result;

        } catch (Exception e) {
            String error = "GET 요청 실패: " + e.getMessage();
            System.err.println(error);
            return error;
        }
    }
    /*-------------------------테스트용 GET요청 함수, 추후 삭제 끝-------------------------*/
}
