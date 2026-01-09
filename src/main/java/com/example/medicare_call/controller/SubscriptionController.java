package com.example.medicare_call.controller;

import com.example.medicare_call.dto.SubscriptionResponse;
import com.example.medicare_call.global.annotation.AuthUser;
import com.example.medicare_call.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "구독 관리", description = "구독 관리 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/elders/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "회원의 어르신 구독 정보 조회")
    @ApiResponse(responseCode = "200", description = "어르신 구독 정보 조회 성공",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = SubscriptionResponse.class)))
    @GetMapping
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptions(@Parameter(hidden = true) @AuthUser Integer memberId) {
        List<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptionsByMember(memberId);
        return ResponseEntity.ok(subscriptions);
    }
}
