package com.example.medicare_call.controller;

import com.example.medicare_call.api.HomeApi;
import com.example.medicare_call.domain.DailyStatistics;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.dto.report.HomeReportResponse;
import com.example.medicare_call.global.annotation.AuthUser;
import com.example.medicare_call.mapper.HomeMapper;
import com.example.medicare_call.service.notification.NotificationService;
import com.example.medicare_call.service.report.HomeReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/elders")
@RequiredArgsConstructor
public class HomeController implements HomeApi {

    private final HomeMapper homeMapper;
    private final HomeReportService homeReportService;
    private final NotificationService notificationService;

    @Override
    @GetMapping("/{elderId}/home")
    public ResponseEntity<HomeReportResponse> getHomeData(@Parameter(hidden = true) @AuthUser Integer memberId, @PathVariable("elderId")Integer elderId) {
        log.info("홈 화면 데이터 조회 요청: elderId={}", elderId);

        Elder elder = homeReportService.getElder(elderId);
        Optional<DailyStatistics> statistics = homeReportService.getTodayStatistics(elder, LocalDate.now());
        List<MedicationSchedule> schedules = homeReportService.getMedicationSchedules(elder);
        int unreadCount = notificationService.getUnreadCount(memberId);

        HomeReportResponse response = homeMapper.mapToHomeReportResponse(
                elder,
                statistics,
                schedules,
                unreadCount,
                LocalTime.now()
        );
        return ResponseEntity.ok(response);
    }
} 