package com.example.medicare_call.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.medicare_call.global.enums.ElderRelation;
import com.example.medicare_call.global.enums.ResidenceType;
import lombok.Setter;

@Entity
@Table(name = "Elder")
@Getter
@NoArgsConstructor
public class Elder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @OneToOne(mappedBy = "elder", cascade = CascadeType.ALL, orphanRemoval = true)
    private Subscription subscription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id", nullable = false)
    private Member guardian;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "gender", nullable = false)
    private Byte gender;

    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship", nullable = false, length = 20)
    private ElderRelation relationship;

    @Enumerated(EnumType.STRING)
    @Column(name = "residence_type", nullable = false, length = 20)
    private ResidenceType residenceType;

    @OneToMany(mappedBy = "elder", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private final List<CareCallRecord> careCallRecords = new ArrayList<>();

    @OneToOne(mappedBy = "elder", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    private CareCallSetting careCallSetting;

    @OneToMany(mappedBy = "elder", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private final List<MedicationSchedule> medicationSchedules = new ArrayList<>();

    @OneToMany(mappedBy = "elder", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private final List<ElderDisease> elderDiseases = new ArrayList<>();

    @OneToOne(mappedBy = "elder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ElderHealthInfo elderHealthInfo;

    @Builder
    public Elder(Integer id, Member guardian, String name, LocalDate birthDate, Byte gender, String phone, ElderRelation relationship, ResidenceType residenceType) {
        this.id = id;
        this.guardian = guardian;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.phone = phone;
        this.relationship = relationship;
        this.residenceType = residenceType;
    }

    // setting update를 post로 구현
    public void applySettings(String name,
                              LocalDate birthDate,
                              Byte gender,
                              String phone,
                              ElderRelation relationship,
                              ResidenceType residenceType) {
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.phone = phone;
        this.relationship = relationship;
        this.residenceType = residenceType;
    }
} 