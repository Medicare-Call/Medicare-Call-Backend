package com.example.medicare_call.service.carecall;

import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.carecall.CareCallSettingRequest;
import com.example.medicare_call.dto.carecall.CareCallSettingResponse;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.global.enums.CallRecurrenceType;
import com.example.medicare_call.repository.CareCallSettingRepository;
import com.example.medicare_call.repository.ElderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CareCallSettingService {
    private final CareCallSettingRepository careCallSettingRepository;
    private final ElderRepository elderRepository;

    @Transactional
    public void upsertCareCallSetting(Integer memberId, Integer elderId, CareCallSettingRequest request){
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND));

        if(!elder.getGuardian().getId().equals(memberId))
            throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);

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
                                    .recurrence(CallRecurrenceType.DAILY.getValue()) //TODO: MVP 단계에서는 daily로 고정
                                    .build();
                            careCallSettingRepository.save(newCareCall);
                        }
                );
    }

    @Transactional(readOnly = true)
    public CareCallSettingResponse getCareCallSetting(Integer memberId, Integer elderId) {
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND));

        if(!elder.getGuardian().getId().equals(memberId))
            throw new CustomException(ErrorCode.HANDLE_ACCESS_DENIED);

        CareCallSetting setting = careCallSettingRepository.findByElder(elder)
                .orElseThrow(() -> new CustomException(ErrorCode.CARE_CALL_SETTING_NOT_FOUND));

        return new CareCallSettingResponse(
                setting.getFirstCallTime(),
                setting.getSecondCallTime(),
                setting.getThirdCallTime()
        );
    }
}
