package com.example.medicare_call.service.carecall.outbound;

import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.carecall.CareCallTestRequest;
import com.example.medicare_call.dto.carecall.ImmediateCareCallRequest.CareCallOption;
import com.example.medicare_call.global.enums.CallType;
import com.example.medicare_call.global.enums.ElderRelation;
import com.example.medicare_call.global.enums.ResidenceType;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.CareCallSettingRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.service.carecall.outbound.client.CareCallClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareCallTestService {

    /** 테스트 발송 시 사용하는 settingId. 실제 CareCallSetting과 무관한 식별용 음수 값 */
    static final int TEST_SETTING_ID = -1;

    private final ElderRepository elderRepository;
    private final CareCallSettingRepository careCallSettingRepository;
    private final CareCallRequestSenderService careCallRequestSenderService;
    private final CareCallClient careCallClient;

    /**
     * 특정 어르신에게 즉시 케어콜을 발송 (베타테스트용)
     * 실제 DB에 등록된 Elder와 CareCallSetting을 기반으로 발송하며,
     * 통화 결과는 실제 케어콜과 동일하게 웹훅을 통해 DB에 저장
     *
     * @param elderId 대상 어르신 ID
     * @param careCallOption 케어콜 옵션 (회차 정보 등)
     * @return 발송 완료 메시지
     */
    @Transactional(readOnly = true)
    public String sendImmediateCall(Long elderId, CareCallOption careCallOption) {
        Elder elder = elderRepository.findById(elderId.intValue())
                .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND, "케어콜 발송 대상 어르신을 찾을 수 없습니다. ID: " + elderId));

        CareCallSetting setting = careCallSettingRepository.findByElder(elder)
                .orElseThrow(() -> new CustomException(ErrorCode.CARE_CALL_SETTING_NOT_FOUND,
                        "즉시 케어콜 발송 대상 어르신의 케어콜 설정을 찾을 수 없습니다. ID: " + elderId));
        CallType callType = convertOptionToCallType(careCallOption);

        careCallRequestSenderService.sendCall(setting.getId(), elderId.intValue(), callType);
        return String.format("%s 어르신께 즉시 케어콜 발송이 완료되었습니다.", elder.getName());
    }

    // TODO: 테스트 서버를 운용하게 될 경우, @Profile로 분리를 권장
    /**
     * 개발자용 케어콜 발신 테스트 (production에서는 미사용)
     * 더미 Elder 데이터를 사용하여 발신 로직만 확인하는 용도입니다.
     * TEST_SETTING_ID(-1)를 사용하므로, 통화 완료 후 웹훅이 수신되어도 DB에 저장되지 않습니다.
     *
     * @param req 테스트할 프롬프트와 전화번호 정보
     */
    public void sendTestCall(CareCallTestRequest req) {
        Elder testElder = Elder.builder()
                .id(100)
                .name("김옥자")
                .phone("01011111111")
                .gender((byte)0)
                .relationship(ElderRelation.CHILD)
                .residenceType(ResidenceType.ALONE)
                .build();

        String testPrompt = req.prompt();
        careCallClient.requestCall(TEST_SETTING_ID, testElder.getId(), req.phoneNumber(), testPrompt);
    }

    /**
     * CareCallOption enum을 CallType enum으로 변환
     * 
     * @param option 변환할 CareCallOption
     * @return 변환된 CallType
     */
    private CallType convertOptionToCallType(CareCallOption option) {
        switch (option) {
            case FIRST: return CallType.FIRST;
            case SECOND: return CallType.SECOND;
            case THIRD: return CallType.THIRD;
            default: throw new IllegalArgumentException("Unknown CareCallOption: " + option);
        }
    }
}
