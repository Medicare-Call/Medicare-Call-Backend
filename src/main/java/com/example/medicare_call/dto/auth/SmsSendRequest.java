package com.example.medicare_call.dto.auth;

import com.example.medicare_call.global.annotation.ValidPhoneNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SmsSendRequest {
    @NotBlank(message = "전화번호는 필수입니다.")
    @ValidPhoneNumber
    private String phone;
}