package com.example.medicare_call.controller;

import com.example.medicare_call.domain.CareCallRecord;
import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.Notification;
import com.example.medicare_call.dto.NotificationDto;
import com.example.medicare_call.global.event.CareCallEvent;
import com.example.medicare_call.global.event.Events;
import com.example.medicare_call.repository.ElderRepository;
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

import java.time.LocalDateTime;
import java.time.LocalTime;

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

    private final ElderRepository elderRepository;
    // TODO 테스트용 api -> 확인 후 제거
    @PostMapping("/test-alarm")
    public ResponseEntity<String> sendHardcodedCareCall() {

        // 1) 어르신 하나 가져온다 (없으면 그냥 가짜로 만든다)
        Elder elder = elderRepository.findById(1)
                .orElseGet(() ->
                        Elder.builder()
                                .id(1)
                                .name("김옥자")
                                .build()
                );

        // 2) 콜 세팅도 하드코딩 (알림 로직이 이걸 본다)
        CareCallSetting setting = CareCallSetting.builder()
                .id(1)
                .elder(elder)
                .firstCallTime(LocalTime.of(8, 0))
                .secondCallTime(LocalTime.of(12, 0))
                .thirdCallTime(LocalTime.of(18, 0))
                .recurrence((byte) 1)
                .build();

        // 3) 실제로 이벤트에 실릴 케어콜 기록을 만든다
        LocalDateTime now = LocalDateTime.now();

        CareCallRecord record = CareCallRecord.builder()
                .elder(elder)
                .setting(setting)
                .calledAt(now)
                .responded((byte) 0)               // 부재중처럼
                .startTime(now)                    // 리스너에서 이거로 차수 계산함
                .endTime(now.plusMinutes(1))
                .callStatus("no-answer")           // ↔ "failed" / "completed"
                .healthStatus((byte) 0)
                .psychStatus((byte) 1)
                .healthDetails("혈압이 평소보다 높고 기침이 2회 이상 감지되었습니다.") // ← null 절대 아님
                .psychologicalDetails("최근 수면시간이 짧아 보입니다.")
                .transcriptionText("AI 콜봇: 응답이 감지되지 않았습니다.")
                .build();

        // 4) 이벤트 발행
        Events.raise(new CareCallEvent(record));

        return ResponseEntity.ok("ok");
    }

}