package com.example.medicare_call.service.carecall;

import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.dto.carecall.CareCallTestRequest;
import com.example.medicare_call.dto.carecall.ImmediateCareCallRequest.CareCallOption;
import com.example.medicare_call.global.enums.CallType;
import com.example.medicare_call.global.enums.ElderRelation;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.global.enums.ResidenceType;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.service.carecall.client.CareCallClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareCallTestService {

    private final ElderRepository elderRepository;
    private final CareCallSettingService careCallSettingService;
    private final CareCallRequestSenderService careCallRequestSenderService;
    private final CareCallClient careCallClient;

    // TODO: KUIT 데모데이 시연용 일시적 기능으로 불완전합니다. 제거 혹은 개선이 필요합니다.
    @Transactional
    public String sendImmediateCall(Long elderId, CareCallOption careCallOption) {
        Elder elder = elderRepository.findById(elderId.intValue())
                .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND, "케어콜 발송 대상 어르신을 찾을 수 없습니다. ID: " + elderId));

        try {
            CareCallSetting setting = careCallSettingService.getOrCreateImmediateSetting(elder);
            CallType callType = convertOptionToCallType(careCallOption);

            careCallRequestSenderService.sendCall(setting.getId(), elderId.intValue(), callType);
            return String.format("%s 어르신께 즉시 케어콜 발송이 완료되었습니다.", elder.getName());
        } catch (Exception e) {
            log.error("즉시 케어콜 발송 실패 - elderId: {}, error: {}", elder.getId(), e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "케어콜 발송 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public void sendTestCall(CareCallTestRequest req) {
        // 테스트용이라 하드코딩된 더미 데이터 사용, DB 저장 안함
        Elder testElder = Elder.builder()
                .id(100)
                .name("김옥자") // 테스트 이름
                .phone("01011111111")
                .gender(Gender.MALE)
                .relationship(ElderRelation.CHILD)
                .residenceType(ResidenceType.ALONE)
                .build();

        String testPrompt = req.prompt();
        // 테스트 호출은 settingId 100으로 가정
        careCallClient.requestCall(100, testElder.getId(), req.phoneNumber(), testPrompt);
    }

    private CallType convertOptionToCallType(CareCallOption option) {
        switch (option) {
            case FIRST: return CallType.FIRST;
            case SECOND: return CallType.SECOND;
            case THIRD: return CallType.THIRD;
            default: throw new IllegalArgumentException("Unknown CareCallOption: " + option);
        }
    }
}
