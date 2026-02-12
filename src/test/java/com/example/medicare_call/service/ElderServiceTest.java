package com.example.medicare_call.service;

import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.domain.MemberElder;
import com.example.medicare_call.dto.ElderRegisterRequest;
import com.example.medicare_call.dto.ElderRegisterResponse;
import com.example.medicare_call.dto.ElderUpdateRequest;
import com.example.medicare_call.global.enums.*;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MemberElderRepository;
import com.example.medicare_call.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

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
    @Mock
    private MemberElderRepository memberElderRepository;
    @InjectMocks
    private ElderService elderService;

    private Member testMember;
    private ElderRegisterRequest testElderRequest1;
    private ElderRegisterRequest testElderRequest2;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1)
                .name("테스트보호자")
                .phone("01000000000")
                .gender(Gender.MALE)
                .termsAgreedAt(LocalDateTime.now())
                .plan(SubscriptionPlan.PREMIUM)
                .build();

        testElderRequest1 = new ElderRegisterRequest();
        testElderRequest1.setName("홍길동");
        testElderRequest1.setBirthDate(LocalDate.of(1940, 5, 1));
        testElderRequest1.setGender(Gender.MALE);
        testElderRequest1.setPhone("01012345678");
        testElderRequest1.setRelationship(ElderRelation.GRANDCHILD);
        testElderRequest1.setResidenceType(ResidenceType.ALONE);

        testElderRequest2 = new ElderRegisterRequest();
        testElderRequest2.setName("김영희");
        testElderRequest2.setBirthDate(LocalDate.of(1945, 3, 15));
        testElderRequest2.setGender(Gender.FEMALE);
        testElderRequest2.setPhone("01098765432");
        testElderRequest2.setRelationship(ElderRelation.CHILD);
        testElderRequest2.setResidenceType(ResidenceType.WITH_FAMILY);
    }

    private byte convertGenderToByte(Gender gender) {
        return (byte) (gender == Gender.MALE ? 0 : 1);
    }

    private Elder createElderFromRequest(Integer id, ElderRegisterRequest request) {
        return Elder.builder()
                .id(id)
                .name(request.getName())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .phone(request.getPhone())
                .relationship(request.getRelationship())
                .residenceType(request.getResidenceType())
                .build();
    }

    private MemberElder createMemberElder(Member guardian, Elder elder, MemberElderAuthority authority) {
        return MemberElder.builder()
                .guardian(guardian)
                .elder(elder)
                .authority(authority)
                .build();
    }

    @Test
    @DisplayName("어르신 등록 성공 - Elder 엔티티 반환")
    void registerElder_success() {
        when(memberRepository.findById(1)).thenReturn(Optional.of(testMember));

        Elder saved = Elder.builder()
            .name(testElderRequest1.getName())
            .build();
        when(elderRepository.save(any(Elder.class))).thenReturn(saved);
        when(memberElderRepository.save(any(MemberElder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MemberElder result = elderService.registerElder(1, testElderRequest1);
        assertThat(result.getElder().getName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("어르신 등록 실패 - 보호자를 찾을 수 없음")
    void registerElder_fail_guardianNotFound() {
        // given
        Integer guardianId = 999;
        when(memberRepository.findById(guardianId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            elderService.registerElder(guardianId, testElderRequest1);
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
        Integer elderId = 1;
        ElderUpdateRequest request = new ElderUpdateRequest("김수정", LocalDate.of(1945, 5, 5), Gender.FEMALE, "01098765432", ElderRelation.CHILD, ResidenceType.WITH_FAMILY);

        Elder elder = Elder.builder().id(elderId).build();
        when(elderRepository.findById(elderId)).thenReturn(Optional.of(elder));
        when(memberElderRepository.findByGuardian_IdAndElder_Id(memberId, elderId))
                .thenReturn(Optional.of(createMemberElder(testMember, elder, MemberElderAuthority.VIEW)));

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
        Integer elderId = 1;

        Elder elder = Elder.builder().id(elderId).build();
        when(elderRepository.findById(elderId)).thenReturn(Optional.of(elder));
        when(memberElderRepository.findByGuardian_IdAndElder_Id(memberId, elderId)).thenReturn(Optional.empty());

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

        Elder elder = Elder.builder()
                .id(elderId)
                .status(ElderStatus.ACTIVATED)
                .build();
        when(elderRepository.findById(elderId)).thenReturn(Optional.of(elder));
        when(memberElderRepository.findByGuardian_IdAndElder_Id(memberId, elderId))
                .thenReturn(Optional.of(createMemberElder(testMember, elder, MemberElderAuthority.MANAGE)));

        // when
        elderService.deleteElder(memberId, elderId);

        // then
        verify(elderRepository, times(1)).delete(elder);
    }

    @Test
    @DisplayName("어르신 일괄 등록 성공")
    void bulkRegisterElders_success() {
        // given
        List<ElderRegisterRequest> requests = List.of(testElderRequest1, testElderRequest2);

        Elder savedElder1 = createElderFromRequest(1, testElderRequest1);
        Elder savedElder2 = createElderFromRequest(2, testElderRequest2);

        when(memberRepository.findById(1)).thenReturn(Optional.of(testMember));
        when(elderRepository.saveAll(any(List.class))).thenReturn(List.of(savedElder1, savedElder2));
        when(memberElderRepository.save(any(MemberElder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        List<ElderRegisterResponse> responses = elderService.bulkRegisterElders(1, requests);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("홍길동");
        assertThat(responses.get(0).getGender()).isEqualTo("MALE");
        assertThat(responses.get(1).getName()).isEqualTo("김영희");
        assertThat(responses.get(1).getGender()).isEqualTo("FEMALE");
        verify(memberRepository, times(1)).findById(1);
        verify(elderRepository, times(1)).saveAll(any(List.class));
    }

    @Test
    @DisplayName("어르신 일괄 등록 실패 - 보호자를 찾을 수 없음")
    void bulkRegisterElders_fail_guardianNotFound() {
        // given
        Integer memberId = 999;
        List<ElderRegisterRequest> requests = List.of(testElderRequest1);

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            elderService.bulkRegisterElders(memberId, requests);
        });
        assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
    }
}
