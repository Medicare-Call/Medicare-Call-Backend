package com.example.medicare_call.controller;

import com.example.medicare_call.service.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 API")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "알림 읽음 상태 변경", description = "알림의 읽음 상태를 업데이트합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "알림 상태 변경 성공"),
            @ApiResponse(responseCode = "404", description = "해당 알림을 찾을 수 없음")
    })
    @PostMapping("/{notificationId}")
    public ResponseEntity<Void> updateNotificationReadStatus(
            @Parameter(description = "알림 ID", required = true, example = "1")
            @PathVariable Long notificationId,
            @RequestBody NotificationReadUpdate request) {
        log.info("알람 읽은 상태 변경 요청: notificationId={}, 상태={}", notificationId, request.isRead());
        notificationService.updateIsRead(notificationId, request.isRead());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Data
    @Schema(description = "알림 읽음 상태 업데이트 요청")
    static class NotificationReadUpdate {
        @Schema(description = "읽음 여부", example = "true")
        private boolean isRead;
    }

}