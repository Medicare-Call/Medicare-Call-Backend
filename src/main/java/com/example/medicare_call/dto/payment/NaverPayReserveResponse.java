package com.example.medicare_call.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "네이버페이 결제 예약 응답")
public class NaverPayReserveResponse {

    @Schema(description = "결과 코드", example = "Success")
    @JsonProperty("code")
    private String code;

    @Schema(description = "결과 메시지", example = "결제 예약이 성공적으로 생성되었습니다.")
    @JsonProperty("message")
    private String message;

    @Schema(description = "응답 본문")
    @JsonProperty("body")
    private ReserveBody body;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "결제 예약 응답 본문")
    public static class ReserveBody {
        @Schema(description = "주문 코드 (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
        @JsonProperty("code")
        private String code;
    }
}
