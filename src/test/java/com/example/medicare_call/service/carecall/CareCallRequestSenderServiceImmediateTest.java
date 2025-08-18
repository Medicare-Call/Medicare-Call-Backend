package com.example.medicare_call.service.carecall;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.global.enums.CallType;
import com.example.medicare_call.global.enums.ElderRelation;
import com.example.medicare_call.global.enums.ResidenceType;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.*;
import com.example.medicare_call.service.carecall.prompt.CallPromptGeneratorFactory;
import com.example.medicare_call.service.carecall.prompt.FirstCallPromptGenerator;
import com.example.medicare_call.service.carecall.prompt.ImmediateCallPromptGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("첫 번째 어르신 즉시 케어콜 요청 서비스 테스트")
class CareCallRequestSenderServiceImmediateTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ElderRepository elderRepository;

    @Mock
    private ElderHealthInfoRepository healthInfoRepository;

    @Mock
    private ElderDiseaseRepository elderDiseaseRepository;

    @Mock
    private MedicationScheduleRepository medicationScheduleRepository;

    @Mock
    private CareCallSettingRepository careCallSettingRepository;

    @Mock
    private CallPromptGeneratorFactory callPromptGeneratorFactory;

    @Mock
    private ImmediateCallPromptGenerator immediateCallPromptGenerator;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CareCallRequestSenderService careCallRequestSenderService;

    private Member testMember;
    private Elder testElder1;
    private Elder testElder2;
    private ElderHealthInfo testHealthInfo;
    private CareCallSetting testCareCallSetting;
    private List<Disease> testDiseases;
    private List<MedicationSchedule> testMedicationSchedules;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1)
                .name("김보호자")
                .phone("01012345678")
                .gender((byte) 1)
                .termsAgreedAt(LocalDateTime.now())
                .plan((byte) 1)
                .build();

        testElder1 = Elder.builder()
                .id(1)
                .guardian(testMember)
                .name("김할머니")
                .birthDate(LocalDate.of(1940, 1, 1))
                .gender((byte) 0)
                .phone("01087654321")
                .relationship(ElderRelation.GRANDCHILD)
                .residenceType(ResidenceType.ALONE)
                .build();

        testElder2 = Elder.builder()
                .id(2)
                .guardian(testMember)
                .name("박할아버지")
                .birthDate(LocalDate.of(1938, 5, 15))
                .gender((byte) 1)
                .phone("01098765432")
                .relationship(ElderRelation.CHILD)
                .residenceType(ResidenceType.ALONE)
                .build();

        testHealthInfo = ElderHealthInfo.builder()
                .id(1)
                .elder(testElder1)
                .build();

        testCareCallSetting = CareCallSetting.builder()
                .id(1)
                .elder(testElder1)
                .firstCallTime(LocalTime.now())
                .recurrence((byte) 0)
                .build();

        testDiseases = Collections.emptyList();
        testMedicationSchedules = Collections.emptyList();

        // Member의 elders 리스트 설정 (testElder1이 첫 번째)
        testMember.getElders().addAll(Arrays.asList(testElder1, testElder2));
    }

    @Test
    @DisplayName("memberId로 첫 번째 어르신에게 즉시 케어콜 발송 - 성공")
    void sendImmediateCallToFirstElder_Success() {
        // given
        Integer memberId = 1;
        String expectedPrompt = "테스트 프롬프트";

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(careCallSettingRepository.findByElder(testElder1)).thenReturn(Optional.of(testCareCallSetting));
        when(careCallSettingRepository.save(any(CareCallSetting.class))).thenReturn(testCareCallSetting);
        when(elderRepository.findById(testElder1.getId())).thenReturn(Optional.of(testElder1));
        when(healthInfoRepository.findByElderId(testElder1.getId())).thenReturn(testHealthInfo);
        when(elderDiseaseRepository.findDiseasesByElder(testElder1)).thenReturn(testDiseases);
        when(medicationScheduleRepository.findByElderId(testElder1.getId())).thenReturn(testMedicationSchedules);
        when(callPromptGeneratorFactory.getGenerator(CallType.IMMEDIATE)).thenReturn(immediateCallPromptGenerator);
        when(immediateCallPromptGenerator.generate(any(), any(), any(), any())).thenReturn(expectedPrompt);

        // when
        String result = careCallRequestSenderService.sendImmediateCallToFirstElder(memberId);

        // then
        assertThat(result).isEqualTo("김할머니 어르신께 즉시 케어콜 발송이 완료되었습니다.");

        verify(memberRepository).findById(memberId);
        verify(careCallSettingRepository).findByElder(testElder1);
        verify(careCallSettingRepository).save(any(CareCallSetting.class));
        verify(callPromptGeneratorFactory).getGenerator(CallType.IMMEDIATE);
        // 첫 번째 어르신에게만 전화하므로 testElder1에만 호출
        verify(elderRepository).findById(testElder1.getId());
        verify(elderRepository, never()).findById(testElder2.getId());
    }

    @Test
    @DisplayName("memberId로 첫 번째 어르신 케어콜 발송 - 회원 없음")
    void sendImmediateCallToFirstElder_MemberNotFound() {
        // given
        Integer memberId = 999;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> careCallRequestSenderService.sendImmediateCallToFirstElder(memberId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("memberId로 첫 번째 어르신 케어콜 발송 - 등록된 어르신 없음")
    void sendImmediateCallToFirstElder_NoElders() {
        // given
        Integer memberId = 1;
        Member memberWithNoElders = Member.builder()
                .id(memberId)
                .name("김보호자")
                .phone("01012345678")
                .gender((byte) 1)
                .termsAgreedAt(LocalDateTime.now())
                .plan((byte) 1)
                .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(memberWithNoElders));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            careCallRequestSenderService.sendImmediateCallToFirstElder(memberId);
        });
        assertEquals(ErrorCode.ELDER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("memberId로 첫 번째 어르신 케어콜 발송 - 새로운 CareCallSetting 생성")
    void sendImmediateCallToFirstElder_CreateNewSetting() {
        // given
        Integer memberId = 1;
        String expectedPrompt = "테스트 프롬프트";

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(careCallSettingRepository.findByElder(testElder1)).thenReturn(Optional.empty()); // 기존 설정 없음
        when(careCallSettingRepository.save(any(CareCallSetting.class))).thenReturn(testCareCallSetting);
        when(elderRepository.findById(testElder1.getId())).thenReturn(Optional.of(testElder1));
        when(healthInfoRepository.findByElderId(testElder1.getId())).thenReturn(testHealthInfo);
        when(elderDiseaseRepository.findDiseasesByElder(testElder1)).thenReturn(testDiseases);
        when(medicationScheduleRepository.findByElderId(testElder1.getId())).thenReturn(testMedicationSchedules);
        when(callPromptGeneratorFactory.getGenerator(CallType.IMMEDIATE)).thenReturn(immediateCallPromptGenerator);
        when(immediateCallPromptGenerator.generate(any(), any(), any(), any())).thenReturn(expectedPrompt);

        // when
        String result = careCallRequestSenderService.sendImmediateCallToFirstElder(memberId);

        // then
        assertThat(result).isEqualTo("김할머니 어르신께 즉시 케어콜 발송이 완료되었습니다.");

        verify(memberRepository).findById(memberId);
        verify(careCallSettingRepository).findByElder(testElder1);
        verify(careCallSettingRepository).save(any(CareCallSetting.class)); // 새로운 설정 저장
        verify(callPromptGeneratorFactory).getGenerator(CallType.IMMEDIATE);
    }
}
