package com.example.medicare_call.service.carecall;

import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.MemberElder;
import com.example.medicare_call.dto.carecall.CareCallSettingRequest;
import com.example.medicare_call.dto.carecall.CareCallSettingResponse;
import com.example.medicare_call.global.enums.CallRecurrenceType;
import com.example.medicare_call.global.enums.MemberElderAuthority;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.CareCallSettingRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MemberElderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class CareCallSettingService {
    private final CareCallSettingRepository careCallSettingRepository;
    private final ElderRepository elderRepository;
    private final MemberElderRepository memberElderRepository;

    /**
     * 회원의 어르신 케어콜 설정을 생성하거나 수정
     * 
     * @param memberId 요청 회원 ID
     * @param elderId 대상 어르신 ID
     * @param request 케어콜 설정 요청 정보 (시간 등)
     */
    @Transactional
    public void upsertCareCallSetting(Integer memberId, Integer elderId, CareCallSettingRequest request){
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND));

        validateManageAuthority(memberId, elderId);

        careCallSettingRepository.findByElder(elder)
                .ifPresentOrElse(
                        careCallSetting -> careCallSetting.update(
                                request.firstCallTime(),
                                request.secondCallTime(),
                                request.thirdCallTime()
                        ),
                        () -> {
                            CareCallSetting newCareCall = CareCallSetting.builder()
                                    .elder(elder)
                                    .firstCallTime(request.firstCallTime())
                                    .secondCallTime(request.secondCallTime())
                                    .thirdCallTime(request.thirdCallTime())
                                    .recurrence(CallRecurrenceType.DAILY) //TODO: MVP 단계에서는 daily로 고정
                                    .build();
                            careCallSettingRepository.save(newCareCall);
                        }
                );
    }

    /**
     * 케어콜 설정을 조회
     * 
     * @param memberId 조회할 회원 ID
     * @param elderId 대상 어르신 ID
     * @return 케어콜 설정 정보
     */
    @Transactional(readOnly = true)
    public CareCallSettingResponse getCareCallSetting(Integer memberId, Integer elderId) {
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND));

        memberElderRepository.findByGuardian_IdAndElder_Id(memberId, elderId)
                .orElseThrow(() -> new CustomException(ErrorCode.HANDLE_ACCESS_DENIED));

        CareCallSetting setting = careCallSettingRepository.findByElder(elder)
                .orElseThrow(() -> new CustomException(ErrorCode.CARE_CALL_SETTING_NOT_FOUND));

        return new CareCallSettingResponse(
                setting.getFirstCallTime(),
                setting.getSecondCallTime(),
                setting.getThirdCallTime()
        );
    }

    /**
     * 회원이 해당 어르신에 대해 관리 권한을 가지고 있는지 검증
     * 
     * @param memberId 회원 ID
     * @param elderId 어르신 ID
     * @throws CustomException 관리 권한이 없는 경우 예외 발생
     */
    private void validateManageAuthority(Integer memberId, Integer elderId) {
        MemberElder relation = memberElderRepository.findByGuardian_IdAndElder_Id(memberId, elderId)
                .orElseThrow(() -> new CustomException(ErrorCode.HANDLE_ACCESS_DENIED));
        if (relation.getAuthority() != MemberElderAuthority.MANAGE) {
            throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);
        }
    }

    /**
     * 즉시 케어콜을 위한 케어콜 설정을 생성하거나 조회
     * 1차 케어콜 시간을 현재 시간으로 설정하여 반환
     * 
     * @param elder 대상 어르신 엔티티
     * @return 케어콜 설정 엔티티
     */
    @Transactional
    public CareCallSetting getOrCreateImmediateSetting(Elder elder) {
        LocalTime currentTime = LocalTime.now().withSecond(0).withNano(0);

        return careCallSettingRepository.findByElder(elder)
            .map(setting -> {
                setting.update(currentTime, setting.getSecondCallTime(), setting.getThirdCallTime());
                return setting;
            })
            .orElseGet(() -> {
                CareCallSetting newSetting = CareCallSetting.builder()
                    .elder(elder)
                    .firstCallTime(currentTime)
                    .recurrence(CallRecurrenceType.DAILY)
                    .build();
                return careCallSettingRepository.save(newSetting);
            });
    }
}
