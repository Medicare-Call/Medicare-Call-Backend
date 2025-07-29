package com.example.medicare_call.dto;

import com.example.medicare_call.global.enums.ElderRelation;
import com.example.medicare_call.global.enums.ResidenceType;
import com.example.medicare_call.global.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ElderRegisterRequest {
    @Schema(description = "어르신 이름", example = "홍길동")
    @NotBlank(message = "어르신 이름은 필수입니다.")
    private String name;

    @Schema(description = "생년월일", example = "1940-05-01")
    @NotNull(message = "생년월일은 필수입니다.")
    private LocalDate birthDate;

    @Schema(description = "성별 (MALE: 남성, FEMALE: 여성)", example = "MALE")
    @NotNull(message = "성별은 필수입니다.")
    private Gender gender;

    @Schema(description = "휴대폰 번호(01012345678 형식)", example = "01012345678")
    @NotBlank(message = "휴대폰 번호는 필수입니다.")
    @Pattern(regexp = "^010\\d{8}$", message = "휴대폰 번호는 01012345678 형식이어야 합니다.")
    private String phone;

    @Schema(description = "어르신과의 관계 (CHILD: 자식, GRANDCHILD: 손자, SIBLING: 형제, RELATIVE: 친척, ACQUAINTANCE: 지인)", example = "GRANDCHILD")
    @NotNull(message = "어르신과의 관계는 필수입니다.")
    private ElderRelation relationship;

    @Schema(description = "어르신 거주방식 (ALONE: 혼자 계세요, WITH_FAMILY: 가족과 함께 살아요)", example = "ALONE")
    @NotNull(message = "어르신 거주방식은 필수입니다.")
    private ResidenceType residenceType;

    @Schema(description = "보호자 ID", example = "1")
    @NotNull(message = "보호자 ID는 필수입니다.")
    private Integer guardianId;
}