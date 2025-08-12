package com.example.medicare_call.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "네이버페이 결제 승인 요청")
public class NaverPayApplyRequest {

    @Schema(description = "네이버페이 결제번호", example = "20170201NP1043587746")
    @NotBlank(message = "결제번호는 필수입니다.")
    @Size(max = 50, message = "결제번호는 50자 이하여야 합니다.")
    private String paymentId;
}
