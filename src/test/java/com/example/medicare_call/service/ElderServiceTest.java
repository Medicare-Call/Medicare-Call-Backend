package com.example.medicare_call.service;

import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.ElderRegisterRequest;
import com.example.medicare_call.global.ResourceNotFoundException;
import com.example.medicare_call.global.enums.ElderRelation;
import com.example.medicare_call.global.enums.ResidenceType;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MemberRepository;
import com.example.medicare_call.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElderServiceTest {
    @Mock
    private ElderRepository elderRepository;
    @Mock
    private MemberRepository memberRepository;
    @InjectMocks
    private ElderService elderService;

    @Test
    @DisplayName("어르신 등록 성공 - Elder 엔티티 반환")
    void registerElder_success() {
        Member member = Member.builder()
            .id(1)
            .name("테스트보호자")
            .phone("01000000000")
            .gender((byte)1)
            .termsAgreedAt(LocalDateTime.now())
            .plan((byte)1)
            .build();
        when(memberRepository.findById(1)).thenReturn(Optional.of(member));

        ElderRegisterRequest req = new ElderRegisterRequest();
        req.setName("홍길동");
        req.setBirthDate(LocalDate.of(1940, 5, 1));
        req.setGender(Gender.MALE);
        req.setPhone("01012345678");
        req.setRelationship(ElderRelation.GRANDCHILD);
        req.setResidenceType(ResidenceType.ALONE);

        Elder saved = Elder.builder()
            .name(req.getName())
            .build();
        when(elderRepository.save(any(Elder.class))).thenReturn(saved);

        Elder result = elderService.registerElder(1, req);
        assertThat(result.getName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("어르신 등록 실패 - 보호자를 찾을 수 없음")
    void registerElder_fail_guardianNotFound() {
        // given
        ElderRegisterRequest req = new ElderRegisterRequest();
        req.setName("홍길동");
        req.setBirthDate(LocalDate.of(1940, 5, 1));
        req.setGender(Gender.MALE);
        req.setPhone("01012345678");
        req.setRelationship(ElderRelation.GRANDCHILD);
        req.setResidenceType(ResidenceType.ALONE);

        when(memberRepository.findById(999)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> elderService.registerElder(999, req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("보호자를 찾을 수 없습니다: 999");
    }
} 