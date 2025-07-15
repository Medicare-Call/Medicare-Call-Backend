package com.example.medicare_call.dto;

import com.example.medicare_call.global.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class RegisterRequest {

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotNull(message = "생년월일은 필수입니다.")
    private LocalDate birthDate;

    @NotNull(message = "성별은 필수입니다.")
    private Gender gender;
}
