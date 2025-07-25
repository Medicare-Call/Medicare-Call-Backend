package com.example.medicare_call.scheduler;

import com.example.medicare_call.service.CareCallSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CareCallScheduler {

    private final CareCallSchedulerService schedulerService;

    @Scheduled(cron = "0 * * * * *") // 매 분 0초마다 실행
    public void runCallScheduler() {
        schedulerService.checkAndSendCalls();
    }
}
