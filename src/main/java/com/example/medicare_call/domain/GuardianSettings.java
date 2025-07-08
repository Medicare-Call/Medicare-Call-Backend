package com.example.medicare_call.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Table(name = "GuardianSettings")
@Getter
@Setter
@NoArgsConstructor
public class GuardianSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id", nullable = false, unique = true)
    private Member guardian;

    @Column(name = "push_alert", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Byte pushAlert;

    @Column(name = "call_complete_alert", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Byte callCompleteAlert;

    @Column(name = "health_alert", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Byte healthAlert;

    @Column(name = "missed_call_alert", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Byte missedCallAlert;
} 