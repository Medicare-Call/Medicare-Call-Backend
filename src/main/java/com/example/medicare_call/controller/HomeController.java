package com.example.medicare_call.controller;

import com.example.medicare_call.api.HomeApi;
import com.example.medicare_call.dto.report.HomeReportResponse;
import com.example.medicare_call.global.annotation.AuthUser;
import com.example.medicare_call.service.notification.NotificationService;
import com.example.medicare_call.service.report.HomeReportService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/elders")
@RequiredArgsConstructor
public class HomeController implements HomeApi {

    private final HomeReportService homeReportService;
    private final NotificationService notificationService;

    @Override
    @GetMapping("/{elderId}/home")
    public ResponseEntity<HomeReportResponse> getHomeData(@Parameter(hidden = true) @AuthUser Integer memberId, @PathVariable("elderId")Integer elderId) {
        log.info("홈 화면 데이터 조회 요청: elderId={}", elderId);

        // 1. 읽지 않은 알림 수 조회
        int unreadCount = notificationService.getUnreadCount(memberId);

        // 2. 서비스를 호출하여 최종 결과 생성
        HomeReportResponse response = homeReportService.getHomeReport(memberId, elderId, unreadCount);

        return ResponseEntity.ok(response);
    }
} 