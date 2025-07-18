package com.example.medicare_call.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@Entity
@Table(name = "Member")
@Getter
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

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

    public Member() {}

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
} 