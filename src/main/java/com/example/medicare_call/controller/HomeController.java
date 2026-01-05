package com.example.medicare_call.controller;

import com.example.medicare_call.api.HomeApi;
import com.example.medicare_call.dto.report.HomeReportResponse;
import com.example.medicare_call.global.annotation.AuthUser;
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

@Slf4j
@RestController
@RequestMapping("/elders")
@RequiredArgsConstructor
@Tag(name = "Home", description = "홈 화면 데이터 조회 API")
public class HomeController implements HomeApi {

    private final HomeReportService homeReportService;

    @Override
    @GetMapping("/{elderId}/home")
    public ResponseEntity<HomeReportResponse> getHomeData(@Parameter(hidden = true)Long memberId, @PathVariable("elderId")Integer elderId) {
        log.info("홈 화면 데이터 조회 요청: elderId={}", elderId);
        HomeReportResponse response = homeReportService.getHomeReport(memberId.intValue(), elderId);
        return ResponseEntity.ok(response);
    }
} 