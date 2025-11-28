package com.example.medicare_call.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.example.medicare_call.global.enums.ElderRelation;
import com.example.medicare_call.global.enums.ElderStatus;
import com.example.medicare_call.global.enums.ResidenceType;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

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

    @JsonManagedReference
    @OneToOne(mappedBy = "elder")
    private Subscription subscription;

    @JsonIgnore
    @OneToMany(mappedBy = "elder", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<MemberElder> memberElders = new ArrayList<>();

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

    @JsonManagedReference
    @OneToMany(mappedBy = "elder")
    private final List<CareCallRecord> careCallRecords = new ArrayList<>();

    @JsonManagedReference
    @OneToOne(mappedBy = "elder", fetch = FetchType.LAZY)
    private CareCallSetting careCallSetting;

    @JsonManagedReference
    @OneToMany(mappedBy = "elder")
    private final List<MedicationSchedule> medicationSchedules = new ArrayList<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "elder")
    private final List<ElderDisease> elderDiseases = new ArrayList<>();

    @JsonManagedReference
    @OneToOne(mappedBy = "elder", fetch = FetchType.LAZY)
    private ElderHealthInfo elderHealthInfo;

    @Builder
    public Elder(Integer id, String name, LocalDate birthDate, Byte gender, String phone, ElderRelation relationship, ResidenceType residenceType, ElderStatus status) {
        this.id = id;
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

    public void addMemberElder(MemberElder memberElder) {
        this.memberElders.add(memberElder);
    }

    public void removeMemberElder(MemberElder memberElder) {
        this.memberElders.remove(memberElder);
    }
}
