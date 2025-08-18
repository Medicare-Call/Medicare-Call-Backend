package com.example.medicare_call.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.medicare_call.global.enums.ElderRelation;
import com.example.medicare_call.global.enums.ElderStatus;
import com.example.medicare_call.global.enums.ResidenceType;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "Elder")
@Getter
@NoArgsConstructor
@Where(clause = "status = 'ACTIVATED'")
@SQLDelete(sql = "UPDATE Elder SET status = 'DELETED' WHERE id = ?")
public class Elder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @OneToOne(mappedBy = "elder")
    private Subscription subscription;

    @JsonBackReference
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ElderStatus status;

    @OneToMany(mappedBy = "elder")
    private final List<CareCallRecord> careCallRecords = new ArrayList<>();

    @OneToOne(mappedBy = "elder", fetch = FetchType.LAZY)
    private CareCallSetting careCallSetting;

    @OneToMany(mappedBy = "elder")
    private final List<MedicationSchedule> medicationSchedules = new ArrayList<>();

    @OneToMany(mappedBy = "elder")
    private final List<ElderDisease> elderDiseases = new ArrayList<>();

    @OneToOne(mappedBy = "elder", fetch = FetchType.LAZY)
    private ElderHealthInfo elderHealthInfo;

    @Builder
    public Elder(Integer id, Member guardian, String name, LocalDate birthDate, Byte gender, String phone, ElderRelation relationship, ResidenceType residenceType, ElderStatus status) {
        this.id = id;
        this.guardian = guardian;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.phone = phone;
        this.relationship = relationship;
        this.residenceType = residenceType;
        this.status = status != null ? status : ElderStatus.ACTIVATED;
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