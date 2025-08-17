package com.example.medicare_call.dto;

import com.example.medicare_call.domain.Member;
import com.example.medicare_call.global.annotation.ValidPhoneNumber;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.global.enums.NotificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MemberInfoUpdateRequest(
        @NotBlank(message = "이름은 필수입니다")
        @Schema(description = "보호자 이름", example = "김미연")
        String name,

        @NotNull(message = "생년월일은 필수입니다")
        @Schema(description = "생년월일", example = "2000-01-01")
        LocalDate birthDate,

        @NotNull(message = "성별은 필수입니다")
        @Schema(description = "성별", example = "FEMALE")
        Gender gender,

        @NotBlank(message = "휴대폰 번호는 필수입니다")
        @ValidPhoneNumber
        @Schema(description = "휴대폰 번호", example = "01012345678")
        String phone,

        @NotNull(message = "푸시 알림 설정은 필수입니다")
        @Schema(description = "푸시 알림 설정 객체")
        PushNotificationUpdateRequest pushNotification
) {
    public record PushNotificationUpdateRequest(
            @NotNull @Schema(description = "전체 푸시 알림 설정", example = "ON")
            NotificationStatus all,

            @NotNull @Schema(description = "케어콜 완료 시 알림 설정", example = "OFF")
            NotificationStatus carecallCompleted,

            @NotNull @Schema(description = "건강 이상 징후 알림 설정", example = "OFF")
            NotificationStatus healthAlert,

            @NotNull @Schema(description = "케어콜 부재중 알림 설정", example = "ON")
            NotificationStatus carecallMissed
    ) {
    }

        public void updateMember(Member member) {
                member.updateInfo(
                        this.name,
                        this.birthDate,
                        this.gender.getCode(), // Gender enum을 byte로 변환
                        this.phone,
                        this.pushNotification.all,
                        this.pushNotification.carecallCompleted,
                        this.pushNotification.healthAlert,
                        this.pushNotification.carecallMissed
                );
        }

}

