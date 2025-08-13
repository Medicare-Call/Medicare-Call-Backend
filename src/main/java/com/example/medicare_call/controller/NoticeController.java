package com.example.medicare_call.controller;

import com.example.medicare_call.dto.NoticeResponse;
import com.example.medicare_call.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
@Tag(name = "공지사항", description = "공지사항 관련 API")
public class NoticeController {
    
    private final NoticeService noticeService;
    
    @GetMapping
    @Operation(summary = "공지사항 목록 조회", description = "모든 공지사항을 게시일 기준 내림차순으로 조회합니다.")
    public ResponseEntity<List<NoticeResponse>> getNotices() {
        log.info("공지사항 목록 조회 요청");
        
        List<NoticeResponse> notices = noticeService.getAllNotices();
        
        return ResponseEntity.ok(notices);
    }
} 