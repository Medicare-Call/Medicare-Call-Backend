package com.example.medicare_call.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;

@Entity
@Table(name = "ElderHealthInfo")
@Getter
@NoArgsConstructor
public class ElderHealthInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "elder_id", nullable = false, unique = true)
    private Elder elder;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Builder
    public ElderHealthInfo(Integer id, Elder elder, String notes) {
        this.id = id;
        this.elder = elder;
        this.notes = notes;
    }
} 