package com.example.medicare_call.domain;

import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.global.enums.NotificationStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.*;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "Member")
@Getter
@NoArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subscription> subscriptions = new ArrayList<>();

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "gender", nullable = false)
    private Byte gender;

    @Column(name = "terms_agreed_at", nullable = false)
    private LocalDateTime termsAgreedAt;

    @Column(name = "plan") //처음 회원가입 시 plan이 없으므로  nullable=false 조건 삭제
    private Byte plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "push_all", nullable = false)
    private NotificationStatus pushAll = NotificationStatus.ON;

    @Enumerated(EnumType.STRING)
    @Column(name = "push_carecall_completed", nullable = false)
    private NotificationStatus pushCarecallCompleted = NotificationStatus.ON;

    @Enumerated(EnumType.STRING)
    @Column(name = "push_health_alert", nullable = false)
    private NotificationStatus pushHealthAlert = NotificationStatus.ON;

    @Enumerated(EnumType.STRING)
    @Column(name = "push_carecall_missed", nullable = false)
    private NotificationStatus pushCarecallMissed = NotificationStatus.ON;

    @OneToMany(mappedBy = "guardian", cascade = CascadeType.ALL, orphanRemoval = true)
    @Where(clause = "status = 'ACTIVATED'")
    private List<Elder> elders = new ArrayList<>();

    public Gender getGenderEnum() {
        return Gender.fromCode(gender); // byte값 → Enum 변환
    }

    @Builder
    public Member(Integer id, String name, String phone, LocalDate birthDate, Byte gender, LocalDateTime termsAgreedAt, Byte plan) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.birthDate = birthDate;
        this.gender = gender;
        this.termsAgreedAt = termsAgreedAt;
        this.plan = plan;
    }

    public void updateInfo(String name, LocalDate birthDate, Byte gender, String phone,
                           NotificationStatus pushAll, NotificationStatus pushCarecallCompleted,
                           NotificationStatus pushHealthAlert, NotificationStatus pushCarecallMissed) {
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.phone = phone;
        this.pushAll = pushAll;
        this.pushCarecallCompleted = pushCarecallCompleted;
        this.pushHealthAlert = pushHealthAlert;
        this.pushCarecallMissed = pushCarecallMissed;
    }
} 