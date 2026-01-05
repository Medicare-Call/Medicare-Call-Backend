package com.example.medicare_call.controller;

import com.example.medicare_call.api.WeeklyStatsApi;
import com.example.medicare_call.dto.report.WeeklyReportResponse;
import com.example.medicare_call.global.annotation.AuthUser;
import com.example.medicare_call.service.report.WeeklyReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/elders")
@RequiredArgsConstructor
public class WeeklyStatsController implements WeeklyStatsApi {

    private final WeeklyReportService weeklyReportService;

    @Override
    @GetMapping("/{elderId}/weekly-stats")
    public ResponseEntity<WeeklyReportResponse> getWeeklyStats(
        @AuthUser Long memberId,
        @PathVariable("elderId") Integer elderId,
        @RequestParam("startDate") LocalDate startDate
    ) {
        log.info("주간 통계 데이터 조회 요청: elderId={}, startDate={}", elderId, startDate);

        WeeklyReportResponse response = weeklyReportService.getWeeklyReport(memberId.intValue(), elderId, startDate);

        return ResponseEntity.ok(response);
    }
} 