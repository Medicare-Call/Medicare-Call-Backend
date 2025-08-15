package com.example.medicare_call.dto;

import com.example.medicare_call.domain.Subscription;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "구독 정보 응답")
public class SubscriptionResponse {

    @Schema(description = "어르신 고유 ID")
    private Integer elderId;

    @Schema(description = "어르신 성함")
    private String name;

    @Schema(description = "구독 플랜 이름")
    private String plan;

    @Schema(description = "월 요금")
    private Integer price;

    @Schema(description = "다음 결제 예정일")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate nextBillingDate;

    @Schema(description = "최초 가입일")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    public static SubscriptionResponse from(Subscription subscription) {
        return SubscriptionResponse.builder()
                .elderId(subscription.getElder().getId())
                .name(subscription.getElder().getName())
                .plan(subscription.getPlan().getPlanName())
                .price(subscription.getPrice())
                .nextBillingDate(subscription.getNextBillingDate())
                .build();
    }
}
