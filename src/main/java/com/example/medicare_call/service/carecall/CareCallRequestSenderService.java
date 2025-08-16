package com.example.medicare_call.service.carecall;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.dto.carecall.CareCallTestRequest;
import com.example.medicare_call.global.enums.CallType;
import com.example.medicare_call.global.enums.ElderRelation;
import com.example.medicare_call.global.enums.ResidenceType;
import com.example.medicare_call.repository.*;
import com.example.medicare_call.util.PhoneNumberUtil;
import com.example.medicare_call.service.carecall.prompt.CallPromptGenerator;
import com.example.medicare_call.service.carecall.prompt.CallPromptGeneratorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;


@Slf4j
@Service
@RequiredArgsConstructor
public class CareCallRequestSenderService {

    private final MemberRepository memberRepository;
    @Value("${care-call.url}")
    private String callUrl;

    private final RestTemplate restTemplate;
    private final ElderRepository elderRepository;
    private final ElderHealthInfoRepository healthInfoRepository;
    private final ElderDiseaseRepository elderDiseaseRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final CareCallSettingRepository careCallSettingRepository;

    private final CallPromptGeneratorFactory callPromptGeneratorFactory;

    public void sendCall(Integer settingId, Integer elderId, CallType callType) {
        System.out.println("Call URL: " + callUrl);
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND, "케어콜 발송 대상 어르신을 찾을 수 없습니다. ID: " + elderId));

        ElderHealthInfo healthInfo = healthInfoRepository.findByElderId(elderId);
        List<Disease> diseases = elderDiseaseRepository.findDiseasesByElder(elder);
        List<MedicationSchedule> medicationSchedules = medicationScheduleRepository.findByElderId(elderId);

        // CallPromptGenerator를 CallType으로부터 선택
        CallPromptGenerator promptGenerator = callPromptGeneratorFactory.getGenerator(callType);

        // 선택된 생성기로 프롬프트 생성
        String prompt = promptGenerator.generate(elder, healthInfo, diseases, medicationSchedules);

        sendPrompt(settingId, elder.getId(), elder.getPhone(), prompt);
    }

    /**
     * // TODO [DEMO] 데모데이 시연용 임시 코드 → 정식 버전 구현 시 제거 필요
     * memberId를 통해 해당 보호자의 첫 번째 어르신에게 즉시 케어콜 발송
     */
    public String sendImmediateCallToFirstElder(Integer memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND, "보호자를 찾을 수 없습니다. ID: " + memberId));

        List<Elder> elders = member.getElders();
        if (elders.isEmpty()) {
            throw new CustomException(ErrorCode.ELDER_NOT_FOUND);
        }

        Elder firstElder = elders.get(0);
        
        try {
            CareCallSetting setting = getOrCreateImmediateSetting(firstElder);
            sendCall(setting.getId(), firstElder.getId(), CallType.FIRST);
            return String.format("%s 어르신께 즉시 케어콜 발송이 완료되었습니다.", firstElder.getName());
        } catch (Exception e) {
            log.error("즉시 케어콜 발송 실패 - elderId: {}, error: {}", firstElder.getId(), e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "케어콜 발송 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * // TODO [DEMO] 데모데이 시연용 임시 코드 → 정식 버전 구현 시 제거 필요
     * 즉시 전화를 위한 CareCallSetting 생성 또는 업데이트
     */
    private CareCallSetting getOrCreateImmediateSetting(Elder elder) {
        LocalTime currentTime = LocalTime.now().withSecond(0).withNano(0);
        
        Optional<CareCallSetting> existingSetting = careCallSettingRepository.findByElder(elder);
        
        if (existingSetting.isPresent()) {
            CareCallSetting setting = existingSetting.get();
            setting.update(currentTime, setting.getSecondCallTime(), setting.getThirdCallTime());
            return careCallSettingRepository.save(setting);
        } else {
            CareCallSetting newSetting = CareCallSetting.builder()
                    .elder(elder)
                    .firstCallTime(currentTime)
                    .recurrence((byte) 0)
                    .build();
            return careCallSettingRepository.save(newSetting);
        }
    }

    //TODO: 개발 완료 후 삭제. 테스트용 member와 elder정보이므로 DB에 저장하지 않는다.
    public void sendTestCall(CareCallTestRequest req){

        Member testMember = Member.builder()
                .id(100)
                .name("테스트 멤버")
                .phone("01000000000")
                .gender((byte) 0)
                .plan((byte) 0)
                .build();
        Elder testElder = Elder.builder()
                .id(100)
                .name("김옥자")
                .phone("01011111111")
                .gender((byte)0)
                .guardian(testMember)
                .relationship(ElderRelation.CHILD)
                .residenceType(ResidenceType.ALONE)
                .build();

        String testPrompt = req.prompt();
        sendPrompt(100, testElder.getId(), req.phoneNumber(), testPrompt);
    }

    private void sendPrompt(Integer settingId, Integer elderId, String phoneNumber, String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String normalizedPhoneNumber = PhoneNumberUtil.normalizeKoreanPhoneNumber(phoneNumber);

            Map<String, Object> body = new HashMap<>();
            body.put("elderId", elderId);
            body.put("settingId", settingId);
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
