package com.example.medicare_call.dto.payment;

import com.example.medicare_call.global.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentApprovalResponse {
    private String orderCode;
    private OrderStatus status;
    private String message;
}
