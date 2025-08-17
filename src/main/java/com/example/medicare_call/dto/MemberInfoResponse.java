package com.example.medicare_call.dto;

import com.example.medicare_call.domain.Member;
import com.example.medicare_call.global.annotation.ValidBirthDate;
import com.example.medicare_call.global.annotation.ValidPhoneNumber;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.global.enums.NotificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record MemberInfoResponse(
        @Schema(description = "보호자 이름", example = "김미연")
        @NotBlank(message = "이름은 필수입니다")
        String name,

        @Schema(description = "생년월일", example = "2000-01-01")
        @NotBlank(message = "생년월일은 필수입니다")
        @ValidBirthDate
        LocalDate birthDate,

        @Schema(description = "성별", example = "FEMALE")
        @NotBlank(message = "성별은 필수입니다")
        Gender gender,

        @Schema(description = "휴대폰 번호", example = "01012345678")
        @ValidPhoneNumber
        @NotBlank(message = "휴대폰 번호는 필수입니다")
        String phone,

        @Schema(description = "푸시 알림 설정 객체")
        @NotBlank(message = "푸시알림 정보는 필수입니다")
        PushNotificationResponse pushNotification
) {
    public record PushNotificationResponse(
            @Schema(description = "전체 푸시 알림 설정", example = "ON", allowableValues = {"ON", "OFF"})
            NotificationStatus all,

            @Schema(description = "케어콜 완료 시 알림 설정", example = "OFF", allowableValues = {"ON", "OFF"})
            NotificationStatus carecallCompleted,

            @Schema(description = "건강 이상 징후 알림 설정", example = "OFF", allowableValues = {"ON", "OFF"})
            NotificationStatus healthAlert,

            @Schema(description = "케어콜 부재중 알림 설정", example = "ON", allowableValues = {"ON", "OFF"})
            NotificationStatus carecallMissed
    ) {
    }

    public static MemberInfoResponse from(Member member) {
        return new MemberInfoResponse(
                member.getName(),
                member.getBirthDate(),
                member.getGenderEnum(),
                member.getPhone(),
                new PushNotificationResponse(
                        member.getPushAll(),
                        member.getPushCarecallCompleted(),
                        member.getPushHealthAlert(),
                        member.getPushCarecallMissed()
                )
        );
    }
}
