package com.example.medicare_call.service;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.ElderHealthInfoCreateRequest;
import com.example.medicare_call.dto.ElderHealthInfoCreateRequestWithElderId;
import com.example.medicare_call.global.enums.ElderHealthNoteType;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.*;
import com.example.medicare_call.service.report.ElderHealthInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ElderHealthInfoServiceTest {
    @Mock ElderRepository elderRepository;
    @Mock ElderHealthInfoRepository elderHealthInfoRepository;
    @Mock ElderDiseaseRepository elderDiseaseRepository;
    @Mock DiseaseRepository diseaseRepository;
    @Mock MedicationScheduleRepository medicationScheduleRepository;
    @Mock MemberRepository memberRepository;
    @InjectMocks
    ElderHealthInfoService elderHealthInfoService;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void registerElderHealthInfo_success() {
        // given
        List<String> diseaseNames = Arrays.asList("고혈압, 당뇨", "치매");
        List<ElderHealthInfoCreateRequest.MedicationScheduleRequest> medicationSchedules = Arrays.asList(
                ElderHealthInfoCreateRequest.MedicationScheduleRequest.builder().medicationName("혈압약").scheduleTimes(Arrays.asList("MORNING", "DINNER")).build(),
                ElderHealthInfoCreateRequest.MedicationScheduleRequest.builder().medicationName("당뇨약").scheduleTimes(Arrays.asList("MORNING, LUNCH")).build()
        );
        ElderHealthInfoCreateRequest request = ElderHealthInfoCreateRequest.builder()
                .notes(List.of(ElderHealthNoteType.INSOMNIA))
                .diseaseNames(diseaseNames)
                .medicationSchedules(medicationSchedules)
                .build();

        when(elderRepository.findById(1)).thenReturn(Optional.of(new Elder()));
        when(diseaseRepository.findByName(anyString())).thenAnswer(invocation -> Optional.of(new Disease()));
        when(diseaseRepository.save(any(Disease.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        elderHealthInfoService.upsertElderHealthInfo(1, request);

        verify(elderDiseaseRepository, times(3)).save(any(ElderDisease.class));
        verify(medicationScheduleRepository, times(4)).save(any(MedicationSchedule.class));
        verify(elderHealthInfoRepository, times(1)).save(any(ElderHealthInfo.class));
    }

    @Test
    @DisplayName("어르신 건강 정보 등록/수정 실패 - 어르신 없음")
    void upsertElderHealthInfo_fail_elderNotFound() {
        // given
        Integer elderId = 99;
        ElderHealthInfoCreateRequest request = new ElderHealthInfoCreateRequest();
        when(elderRepository.findById(elderId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            elderHealthInfoService.upsertElderHealthInfo(elderId, request);
        });
        assertEquals(ErrorCode.ELDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("어르신 건강 정보 조회 실패 - 보호자 없음")
    void getElderHealth_fail_memberNotFound() {
        // given
        Integer memberId = 99;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            elderHealthInfoService.getElderHealth(memberId);
        });
        assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
    }

//    @Test
//    @DisplayName("어르신 건강 정보 조회 - 어르신이 없는 경우 빈 리스트 반환")
//    void getElderHealth_empty_noElder() {
//        // given
//        Member member = Member.builder().id(1).build();
//        when(memberRepository.findById(1)).thenReturn(Optional.of(member));
//        when(elderRepository.findByGuardian(member)).thenReturn(List.of()); // 어르신 없음
//
//        // when
//        List<ElderHealthInfoResponse> result = elderHealthInfoService.getElderHealth(1);
//
//        // then
//        assertEquals(0, result.size());
//    }
//
//    @Test
//    @DisplayName("어르신 건강 정보 조회 - 어르신은 있지만 모든 데이터가 비어있는 경우")
//    void getElderHealth_empty_fields() {
//        // given
//        Member member = Member.builder().id(1).build();
//        Elder elder = Elder.builder()
//                .id(10)
//                .name("김어르신")
//                .elderDiseases(List.of()) // 질병 없음
//                .medicationSchedules(List.of()) // 약 정보 없음
//                .elderHealthInfo(null) // 특이사항 없음
//                .build();
//
//        when(memberRepository.findById(1)).thenReturn(Optional.of(member));
//        when(elderRepository.findByGuardian(member)).thenReturn(List.of(elder));
//
//        // when
//        List<ElderHealthInfoResponse> result = elderHealthInfoService.getElderHealth(1);
//
//        // then
//        assertEquals(1, result.size());
//        ElderHealthInfoResponse res = result.get(0);
//        assertEquals(10, res.getElderId());
//        assertEquals("김어르신", res.getElderName());
//        assertEquals(List.of(), res.getDiseases());
//        assertEquals(Map.of(), res.getMedications());
//        assertEquals(List.of(), res.getNotes());
//    }

    @Test
    @DisplayName("어르신 건강정보 일괄 등록 성공")
    void bulkUpsertElderHealthInfo_success() {
        // given
        Integer memberId = 1;
        Member member = Member.builder().id(memberId).build();
        Elder elder1 = Elder.builder().id(1).guardian(member).build();
        Elder elder2 = Elder.builder().id(2).guardian(member).build();

        ElderHealthInfoCreateRequestWithElderId request1 = ElderHealthInfoCreateRequestWithElderId.builder()
                .elderId(1)
                .diseaseNames(List.of("고혈압"))
                .notes(List.of(ElderHealthNoteType.FORGET_MEDICATION))
                .build();

        ElderHealthInfoCreateRequestWithElderId request2 = ElderHealthInfoCreateRequestWithElderId.builder()
                .elderId(2)
                .diseaseNames(List.of("당뇨병"))
                .notes(List.of(ElderHealthNoteType.INSOMNIA))
                .build();

        List<ElderHealthInfoCreateRequestWithElderId> requests = List.of(request1, request2);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(elderRepository.findById(1)).thenReturn(Optional.of(elder1));
        when(elderRepository.findById(2)).thenReturn(Optional.of(elder2));
        when(diseaseRepository.findByName(anyString())).thenAnswer(invocation -> Optional.of(new Disease()));
        when(diseaseRepository.save(any(Disease.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        elderHealthInfoService.bulkUpsertElderHealthInfo(memberId, requests);

        // then
        verify(elderDiseaseRepository, times(2)).save(any(ElderDisease.class));
        verify(elderHealthInfoRepository, times(2)).save(any(ElderHealthInfo.class));
    }

    @Test
    @DisplayName("어르신 건강정보 일괄 등록 실패 - 보호자를 찾을 수 없음")
    void bulkUpsertElderHealthInfo_fail_memberNotFound() {
        // given
        Integer memberId = 999;
        List<ElderHealthInfoCreateRequestWithElderId> requests = List.of(
                ElderHealthInfoCreateRequestWithElderId.builder().elderId(1).build()
        );

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            elderHealthInfoService.bulkUpsertElderHealthInfo(memberId, requests);
        });
        assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("어르신 건강정보 일괄 등록 실패 - 어르신을 찾을 수 없음")
    void bulkUpsertElderHealthInfo_fail_elderNotFound() {
        // given
        Integer memberId = 1;
        Member member = Member.builder().id(memberId).build();
        List<ElderHealthInfoCreateRequestWithElderId> requests = List.of(
                ElderHealthInfoCreateRequestWithElderId.builder().elderId(99).build()
        );

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(elderRepository.findById(99)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            elderHealthInfoService.bulkUpsertElderHealthInfo(memberId, requests);
        });
        assertEquals(ErrorCode.ELDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("어르신 건강정보 일괄 등록 실패 - 권한 없음")
    void bulkUpsertElderHealthInfo_fail_accessDenied() {
        // given
        Integer memberId = 1;
        Integer otherMemberId = 2;
        Member member = Member.builder().id(memberId).build();
        Member otherMember = Member.builder().id(otherMemberId).build();
        Elder elder = Elder.builder().id(1).guardian(otherMember).build(); // 다른 보호자의 어르신

        List<ElderHealthInfoCreateRequestWithElderId> requests = List.of(
                ElderHealthInfoCreateRequestWithElderId.builder().elderId(1).build()
        );

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(elderRepository.findById(1)).thenReturn(Optional.of(elder));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            elderHealthInfoService.bulkUpsertElderHealthInfo(memberId, requests);
        });
        assertEquals(ErrorCode.HANDLE_ACCESS_DENIED, exception.getErrorCode());
    }
} 