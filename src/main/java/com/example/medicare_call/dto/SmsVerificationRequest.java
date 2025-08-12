package com.example.medicare_call.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SmsVerificationRequest {
    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^010\\d{8}$", message = "전화번호는 01012345678 형식이어야 합니다.")
    private String phone;

    @NotBlank(message = "인증번호는 필수입니다.")
    @Size(min = 6, max = 6, message = "인증번호는 6자리입니다.")
    private String certificationCode;
}
