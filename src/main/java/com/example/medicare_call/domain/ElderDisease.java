package com.example.medicare_call.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import jakarta.persistence.*;

@Entity
@Table(name = "ElderDisease")
@IdClass(ElderDiseaseId.class)
@Getter
@NoArgsConstructor
public class ElderDisease {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elder_id", nullable = false)
    private Elder elder;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disease_id", nullable = false)
    private Disease disease;

    @Builder
    public ElderDisease(Elder elder, Disease disease) {
        this.elder = elder;
        this.disease = disease;
    }
} 