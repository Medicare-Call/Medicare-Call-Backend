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
import com.example.medicare_call.dto.carecall.ImmediateCareCallRequest.CareCallOption;
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
@DisplayName("즉시 케어콜 요청 서비스 테스트")
class CareCallRequestSenderServiceImmediateTest {

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
    private FirstCallPromptGenerator firstCallPromptGenerator;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CareCallRequestSenderService careCallRequestSenderService;

    private Elder testElder;
    private ElderHealthInfo testHealthInfo;
    private CareCallSetting testCareCallSetting;
    private List<Disease> testDiseases;
    private List<MedicationSchedule> testMedicationSchedules;

    @BeforeEach
    void setUp() {
        Member testMember = Member.builder().id(1).name("김보호자").build();

        testElder = Elder.builder()
                .id(1)
                .name("김할머니")
                .phone("01087654321")
                .build();

        testHealthInfo = ElderHealthInfo.builder().id(1).elder(testElder).build();

        testCareCallSetting = CareCallSetting.builder()
                .id(1)
                .elder(testElder)
                .firstCallTime(LocalTime.now())
                .recurrence((byte) 0)
                .build();

        testDiseases = Collections.emptyList();
        testMedicationSchedules = Collections.emptyList();
    }

    @Test
    @DisplayName("지정된 어르신에게 즉시 케어콜 발송 - 성공")
    void sendImmediateCall_Success() {
        // given
        Long elderId = 1L;
        CareCallOption option = CareCallOption.FIRST;
        String expectedPrompt = "테스트 프롬프트";

        when(elderRepository.findById(elderId.intValue())).thenReturn(Optional.of(testElder));
        when(careCallSettingRepository.findByElder(testElder)).thenReturn(Optional.of(testCareCallSetting));
        when(careCallSettingRepository.save(any(CareCallSetting.class))).thenReturn(testCareCallSetting);
        when(healthInfoRepository.findByElderId(testElder.getId())).thenReturn(testHealthInfo);
        when(elderDiseaseRepository.findDiseasesByElder(testElder)).thenReturn(testDiseases);
        when(medicationScheduleRepository.findByElderId(testElder.getId())).thenReturn(testMedicationSchedules);
        when(callPromptGeneratorFactory.getGenerator(CallType.FIRST)).thenReturn(firstCallPromptGenerator);
        when(firstCallPromptGenerator.generate(any(), any(), any(), any())).thenReturn(expectedPrompt);

        // when
        String result = careCallRequestSenderService.sendImmediateCall(elderId, option);

        // then
        assertThat(result).isEqualTo("김할머니 어르신께 즉시 케어콜 발송이 완료되었습니다.");
        verify(callPromptGeneratorFactory).getGenerator(CallType.FIRST);
    }

    @Test
    @DisplayName("어르신 케어콜 발송 - 어르신 없음")
    void sendImmediateCall_ElderNotFound() {
        // given
        Long elderId = 999L;
        CareCallOption option = CareCallOption.FIRST;
        when(elderRepository.findById(elderId.intValue())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> careCallRequestSenderService.sendImmediateCall(elderId, option))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ELDER_NOT_FOUND);
    }
}
