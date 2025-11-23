package com.example.medicare_call.domain;

import com.example.medicare_call.global.enums.MemberElderAuthority;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "Member_Elder",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_member_elder_member_id_elder_id", columnNames = {"member_id", "elder_id"})
        }
)
@Getter
@NoArgsConstructor
public class MemberElder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_elder_id")
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guardian_id", nullable = false)
    private Member guardian;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "elder_id", nullable = false)
    private Elder elder;

    @Enumerated(EnumType.STRING)
    @Column(name = "authority", nullable = false, length = 20)
    private MemberElderAuthority authority;

    @Builder
    public MemberElder(Member guardian, Elder elder, MemberElderAuthority authority) {
        this.guardian = guardian;
        this.elder = elder;
        this.authority = authority != null ? authority : MemberElderAuthority.VIEW;
    }

    public void changeAuthority(MemberElderAuthority authority) {
        this.authority = authority;
    }
}
