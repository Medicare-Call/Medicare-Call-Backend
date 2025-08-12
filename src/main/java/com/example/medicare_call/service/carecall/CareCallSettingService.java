package com.example.medicare_call.service.carecall;

import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.carecall.CareCallSettingRequest;
import com.example.medicare_call.global.enums.CallRecurrenceType;
import com.example.medicare_call.repository.CareCallSettingRepository;
import com.example.medicare_call.repository.ElderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CareCallSettingService {
    private final CareCallSettingRepository careCallSettingRepository;
    private final ElderRepository elderRepository;

    @Transactional
    public void settingCareCall(Integer elderId, CareCallSettingRequest request){
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID로 등록된 어르신을 찾을 수 없습니다.: " + elderId));

        CareCallSetting newCareCall = CareCallSetting.builder()
                .elder(elder)
                .firstCallTime(request.firstCallTime())
                .secondCallTime(request.secondCallTime())
                .thirdCallTime(request.thirdCallTime())
                .recurrence(CallRecurrenceType.DAILY.getValue()) //TODO: MVP 단계에서는 daily로 고정
                .build();

        careCallSettingRepository.save(newCareCall);
    }
}
