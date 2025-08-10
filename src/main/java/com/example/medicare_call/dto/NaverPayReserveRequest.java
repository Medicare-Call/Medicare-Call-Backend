package com.example.medicare_call.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "네이버페이 결제 예약 요청")
public class NaverPayReserveRequest {

    @Schema(description = "대표 상품명", example = "메디케어콜 스탠다드 플랜")
    @JsonProperty("productName")
    @NotBlank(message = "상품명은 필수입니다.")
    @Size(max = 128, message = "상품명은 128자 이하여야 합니다.")
    private String productName;

    @Schema(description = "상품 수량", example = "1")
    @JsonProperty("productCount")
    @NotNull(message = "상품 수량은 필수입니다.")
    @Min(value = 1, message = "상품 수량은 1 이상이어야 합니다.")
    private Integer productCount;

    @Schema(description = "총 결제 금액", example = "19000")
    @JsonProperty("totalPayAmount")
    @NotNull(message = "총 결제 금액은 필수입니다.")
    @Min(value = 10, message = "총 결제 금액은 10원 이상이어야 합니다.")
    private Integer totalPayAmount;

    @Schema(description = "과세 금액", example = "15000")
    @JsonProperty("taxScopeAmount")
    @NotNull(message = "과세 금액은 필수입니다.")
    @Min(value = 0, message = "과세 금액은 0원 이상이어야 합니다.")
    private Integer taxScopeAmount;

    @Schema(description = "면세 금액", example = "4000")
    @JsonProperty("taxExScopeAmount")
    @NotNull(message = "면세 금액은 필수입니다.")
    @Min(value = 0, message = "면세 금액은 0원 이상이어야 합니다.")
    private Integer taxExScopeAmount;

    @Schema(description = "결제 대상 어르신 ID 목록", example = "[1, 2, 3]")
    @JsonProperty("elderIds")
    @NotNull(message = "어르신 ID 목록은 필수입니다.")
    @Size(min = 1, message = "어르신 ID 목록은 최소 1개 이상이어야 합니다.")
    private List<Long> elderIds;
}
