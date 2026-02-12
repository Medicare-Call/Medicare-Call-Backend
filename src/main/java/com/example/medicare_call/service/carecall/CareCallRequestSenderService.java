package com.example.medicare_call.service.carecall;

import com.example.medicare_call.domain.Disease;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.ElderHealthInfo;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.global.enums.CallType;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.ElderDiseaseRepository;
import com.example.medicare_call.repository.ElderHealthInfoRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MedicationScheduleRepository;
import com.example.medicare_call.service.carecall.client.CareCallClient;
import com.example.medicare_call.service.carecall.prompt.CallPromptGenerator;
import com.example.medicare_call.service.carecall.prompt.CallPromptGeneratorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareCallRequestSenderService {

    private final ElderRepository elderRepository;
    private final ElderHealthInfoRepository healthInfoRepository;
    private final ElderDiseaseRepository elderDiseaseRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final CallPromptGeneratorFactory callPromptGeneratorFactory;
    private final CareCallClient careCallClient;

    /**
     * 특정 어르신에게 케어콜을 발송
     * 어르신의 질병, 건강 정보, 복약 일정을 조회하여 적절한 프롬프트를 생성하고 CareCallClient에 전달한다
     * 
     * @param settingId 케어콜 설정 ID
     * @param elderId 대상 어르신 ID
     * @param callType 케어콜 회차 (1차, 2차, 3차)
     */
    public void sendCall(Integer settingId, Integer elderId, CallType callType) {
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND, "케어콜 발송 대상 어르신을 찾을 수 없습니다. ID: " + elderId));

        ElderHealthInfo healthInfo = healthInfoRepository.findByElderId(elderId);
        List<Disease> diseases = elderDiseaseRepository.findDiseasesByElder(elder);
        List<MedicationSchedule> medicationSchedules = medicationScheduleRepository.findByElderId(elderId);

        // CallPromptGenerator를 CallType으로부터 선택
        CallPromptGenerator promptGenerator = callPromptGeneratorFactory.getGenerator(callType);

        // 선택된 생성기로 프롬프트 생성
        String prompt = promptGenerator.generate(elder, healthInfo, diseases, medicationSchedules);

        careCallClient.requestCall(settingId, elder.getId(), elder.getPhone(), prompt);
    }
}
