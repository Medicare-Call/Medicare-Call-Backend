package com.example.medicare_call.dto.auth;

import com.example.medicare_call.global.annotation.ValidBirthDate;
import com.example.medicare_call.global.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MemberRegisterRequest {

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotNull(message = "생년월일은 필수입니다.")
    @ValidBirthDate
    private LocalDate birthDate;

    @NotNull(message = "성별은 필수입니다.")
    private Gender gender;

    @NotBlank(message = "fcm token은 필수입니다")
    private String fcmToken;

}
