package com.example.medicare_call.service.carecall.outbound;

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

    /**
     * 현재 시간 기준으로 발송 대상인 케어콜을 확인하고 발송
     * 10분 배치 처리를 위해 현재 시각으로부터 10분 전까지의 예약 건을 조회
     */
    public void checkAndSendCalls() {
        LocalTime endTime = LocalTime.now().withSecond(0).withNano(0);
        // 현재부터 10분 전까지의 기간 설정 (cron job 이 10분 단위로 실행)
        LocalTime startTime = endTime.minusMinutes(10);
        checkAndSendCallsInRange(startTime, endTime);
    }

    /**
     * 특정 시간 범위 내에 예약된 케어콜을 조회하여 순차적으로 발송
     * 1차, 2차, 3차 케어콜 설정을 각각 조회
     * 
     * @param startTime 조회 시작 시간
     * @param endTime 조회 종료 시간
     */
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
