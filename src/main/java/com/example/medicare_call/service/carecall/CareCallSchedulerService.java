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
        LocalTime endTime = LocalTime.now().withSecond(0).withNano(0);
        // 현재부터 10분 전까지의 기간 설정 (cron job 이 10분 단위로 실행)
        LocalTime startTime = endTime.minusMinutes(10);
        checkAndSendCallsInRange(startTime, endTime);
    }

    public void checkAndSendCallsInRange(LocalTime startTime, LocalTime endTime) {
        //1차 케어콜
        List<CareCallSetting> firstTargets = settingRepository.findByFirstCallTimeBetween(startTime, endTime);
        for (CareCallSetting setting : firstTargets) {
            careCallRequestSenderService.sendCall(setting.getId(), setting.getElder().getId(), CallType.FIRST);
        }

        //2차 케어콜
        List<CareCallSetting> secondTargets = settingRepository.findBySecondCallTimeBetween(startTime, endTime);
        for (CareCallSetting setting : secondTargets) {
            careCallRequestSenderService.sendCall(setting.getId(), setting.getElder().getId(), CallType.SECOND);
        }

        //3차 케어콜
        List<CareCallSetting> thirdTargets = settingRepository.findByThirdCallTimeBetween(startTime, endTime);
        for (CareCallSetting setting : thirdTargets) {
            careCallRequestSenderService.sendCall(setting.getId(), setting.getElder().getId(), CallType.THIRD);
        }
    }
}
