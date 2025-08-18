package com.example.medicare_call.service.carecall;

import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.global.enums.CallType;
import com.example.medicare_call.repository.CareCallSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CareCallSchedulerService {

    private final CareCallSettingRepository settingRepository;
    private final CareCallRequestSenderService careCallRequestSenderService;

    public void checkAndSendCalls() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0); // UTC 기준 현재 시간
        checkAndSendCalls(now);
    }

    public void checkAndSendCalls(LocalTime now) {
        //1차 케어콜
        List<CareCallSetting> firstTargets = settingRepository.findByFirstCallTime(now);
        for (CareCallSetting setting : firstTargets) {
            careCallRequestSenderService.sendCall(setting.getId(), setting.getElder().getId(), CallType.FIRST);
        }

        //2차 케어콜
        List<CareCallSetting> secondTargets = settingRepository.findBySecondCallTime(now);
        for (CareCallSetting setting : secondTargets) {
            careCallRequestSenderService.sendCall(setting.getId(), setting.getElder().getId(), CallType.SECOND);
        }

        //3차 케어콜
        List<CareCallSetting> thirdTargets = settingRepository.findByThirdCallTime(now);
        for (CareCallSetting setting : thirdTargets) {
            careCallRequestSenderService.sendCall(setting.getId(), setting.getElder().getId(), CallType.THIRD);
        }
    }
}
