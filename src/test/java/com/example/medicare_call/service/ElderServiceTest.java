package com.example.medicare_call.service;

import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.ElderRegisterRequest;
import com.example.medicare_call.global.enums.ElderRelation;
import com.example.medicare_call.global.enums.ElderStatus;
import com.example.medicare_call.global.enums.ResidenceType;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MemberRepository;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.dto.ElderUpdateRequest;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        Integer guardianId = 999;
        ElderRegisterRequest request = new ElderRegisterRequest();
        request.setName("홍길동");
        request.setBirthDate(LocalDate.of(1940, 5, 1));
        request.setGender(Gender.MALE);
        request.setPhone("01012345678");
        request.setRelationship(ElderRelation.GRANDCHILD);
        request.setResidenceType(ResidenceType.ALONE);

        when(memberRepository.findById(guardianId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            elderService.registerElder(guardianId, request);
        });
        assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("어르신 정보 수정 실패 - 존재하지 않는 어르신")
    void updateElder_fail_elderNotFound() {
        // given
        Integer memberId = 1;
        Integer elderId = 99;
        ElderUpdateRequest request = new ElderUpdateRequest("김수정", LocalDate.of(1945, 5, 5), Gender.FEMALE, "01098765432", ElderRelation.CHILD, ResidenceType.WITH_FAMILY);
        when(elderRepository.findById(elderId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            elderService.updateElder(memberId, elderId, request);
        });
        assertEquals(ErrorCode.ELDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("어르신 정보 수정 실패 - 권한 없음")
    void updateElder_fail_accessDenied() {
        // given
        Integer memberId = 1;
        Integer otherMemberId = 2;
        Integer elderId = 1;
        ElderUpdateRequest request = new ElderUpdateRequest("김수정", LocalDate.of(1945, 5, 5), Gender.FEMALE, "01098765432", ElderRelation.CHILD, ResidenceType.WITH_FAMILY);

        Member guardian = Member.builder().id(otherMemberId).build();
        Elder elder = Elder.builder().id(elderId).guardian(guardian).build();
        when(elderRepository.findById(elderId)).thenReturn(Optional.of(elder));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            elderService.updateElder(memberId, elderId, request);
        });
        assertEquals(ErrorCode.HANDLE_ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("어르신 정보 삭제 실패 - 존재하지 않는 어르신")
    void deleteElder_fail_elderNotFound() {
        // given
        Integer memberId = 1;
        Integer elderId = 99;
        when(elderRepository.findById(elderId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            elderService.deleteElder(memberId, elderId);
        });
        assertEquals(ErrorCode.ELDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("어르신 정보 삭제 실패 - 권한 없음")
    void deleteElder_fail_accessDenied() {
        // given
        Integer memberId = 1;
        Integer otherMemberId = 2;
        Integer elderId = 1;

        Member guardian = Member.builder().id(otherMemberId).build();
        Elder elder = Elder.builder().id(elderId).guardian(guardian).build();
        when(elderRepository.findById(elderId)).thenReturn(Optional.of(elder));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            elderService.deleteElder(memberId, elderId);
        });
        assertEquals(ErrorCode.HANDLE_ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("어르신 정보 삭제 성공 (soft delete)")
    void deleteElder_success_softDelete() {
        // given
        Integer memberId = 1;
        Integer elderId = 1;

        Member guardian = Member.builder().id(memberId).build();
        Elder elder = Elder.builder()
                .id(elderId)
                .guardian(guardian)
                .status(ElderStatus.ACTIVATED)
                .build();
        when(elderRepository.findById(elderId)).thenReturn(Optional.of(elder));

        // when
        elderService.deleteElder(memberId, elderId);

        // then
        verify(elderRepository, times(1)).delete(elder);
    }
} 