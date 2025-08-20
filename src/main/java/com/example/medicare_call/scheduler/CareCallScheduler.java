package com.example.medicare_call.scheduler;

import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.repository.CareCallSettingRepository;
import com.example.medicare_call.service.carecall.CareCallSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class CareCallScheduler {

    private final CareCallSchedulerService schedulerService;

    @Scheduled(cron = "0 */10 * * * *") // 매 10분마다 0초에 실행
    public void runCallScheduler() {
        schedulerService.checkAndSendCalls();
    }
}
