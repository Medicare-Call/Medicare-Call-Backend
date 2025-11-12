package com.example.medicare_call.controller;

import com.example.medicare_call.domain.Member;
import com.example.medicare_call.domain.Notification;
import com.example.medicare_call.global.annotation.AuthUser;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.service.MemberService;
import com.example.medicare_call.service.notification.FirebaseService;
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
import retrofit2.Response;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 API")
public class NotificationController {

    private final NotificationService notificationService;
    private final MemberService memberService;

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

    private final FirebaseService firebaseService;

    @PostMapping("/test-alarm")
    @Operation(
            summary = "알림 전송 테스트",
            description = "FCM 알림을 전송하는 테스트용 API입니다. 추후 제거 예정."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "알림 전송 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<Void> sendHardcodedCareCall() {
        log.info("테스트 알람 전송 완료");
        Notification notification = Notification.builder()
                .id(1L)
                .member(Member.builder().id(1234).build())
                .createdAt(LocalDateTime.now())
                .title("테스트 알림 타이틀")
                .body("테스트 알림 바디")
                .isRead(false)
                .build();
        firebaseService.sendNotification(notification);

        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "FCM 토큰 검증",
        description = "사용자의 FCM 토큰이 유효한지 검증하는 API입니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "토큰 검증 성공"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "유효하지 않은 토큰"
        )
    })
    @PostMapping("/validation-token")
    public ResponseEntity<Void> validationToken(
        @Parameter(hidden = true) @AuthUser Long memberId
    ){
        String fcmToken = memberService.getFcmToken(memberId.intValue());
        try {
            firebaseService.validationToken(fcmToken);
        }catch (CustomException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok().build();
    }

}