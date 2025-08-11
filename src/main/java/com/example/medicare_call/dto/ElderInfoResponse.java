package com.example.medicare_call.dto;

import com.example.medicare_call.global.annotation.ValidBirthDate;
import com.example.medicare_call.global.annotation.ValidPhoneNumber;
import com.example.medicare_call.global.enums.ElderRelation;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.global.enums.ResidenceType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;


@Schema(description = "어르신 개인정보 응답")
public record ElderInfoResponse(

        @Schema(description = "어르신 ID", example = "1")
        Integer elderId,

        @Schema(description = "어르신 이름", example = "홍길동")
        String name,

        @ValidBirthDate
        @Schema(description = "생년월일", example = "1945-08-15")
        LocalDate birthDate,

        @Schema(description = "성별", example = "MALE")
        Gender gender,

        @ValidPhoneNumber
        @Schema(description = "전화번호", example = "010-1234-5678")
        String phone,

        @Schema(description = "관계", example = "CHILD")
        ElderRelation relationship,

        @Schema(description = "거주 형태", example = "ALONE")
        ResidenceType residenceType
) {
}
