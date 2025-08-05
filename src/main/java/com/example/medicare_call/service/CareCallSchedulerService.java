package com.example.medicare_call.service;

import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.global.enums.CallType;
import com.example.medicare_call.repository.CareCallSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CareCallSchedulerService {

    private final CareCallSettingRepository settingRepository;
    private final CareCallService callRequestSender;

    public void checkAndSendCalls() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0); // 초 단위 무시

        //1차 케어콜
        List<CareCallSetting> firstTargets = settingRepository.findByFirstCallTime(now);
        for (CareCallSetting setting : firstTargets) {
            callRequestSender.sendCall(setting.getElder().getId(), CallType.FIRST);
        }

        //2차 케어콜
        List<CareCallSetting> secondTargets = settingRepository.findBySecondCallTime(now);
        for (CareCallSetting setting : secondTargets) {
            callRequestSender.sendCall(setting.getElder().getId(), CallType.SECOND);
        }

        //3차 케어콜
        List<CareCallSetting> thirdTargets = settingRepository.findBySecondCallTime(now);
        for (CareCallSetting setting : thirdTargets) {
            callRequestSender.sendCall(setting.getElder().getId(), CallType.THIRD);
        }
    }
}
