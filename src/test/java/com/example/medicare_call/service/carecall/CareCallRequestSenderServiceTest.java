package com.example.medicare_call.service.carecall;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.global.enums.CallType;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.ElderDiseaseRepository;
import com.example.medicare_call.repository.ElderHealthInfoRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MedicationScheduleRepository;
import com.example.medicare_call.service.carecall.client.CareCallClient;
import com.example.medicare_call.service.carecall.prompt.CallPromptGenerator;
import com.example.medicare_call.service.carecall.prompt.CallPromptGeneratorFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("케어콜 발송 서비스 단위 테스트")
class CareCallRequestSenderServiceTest {

    @Mock
    private ElderRepository elderRepository;
    @Mock
    private ElderHealthInfoRepository healthInfoRepository;
    @Mock
    private ElderDiseaseRepository elderDiseaseRepository;
    @Mock
    private MedicationScheduleRepository medicationScheduleRepository;
    @Mock
    private CallPromptGeneratorFactory callPromptGeneratorFactory;
    @Mock
    private CareCallClient careCallClient;
    @Mock
    private CallPromptGenerator promptGenerator;

    @InjectMocks
    private CareCallRequestSenderService careCallRequestSenderService;

    @Test
    @DisplayName("케어콜 발송 성공")
    void sendCall_success() {
        // given
        Integer settingId = 10;
        Integer elderId = 1;
        CallType callType = CallType.FIRST;
        
        Elder elder = Elder.builder().id(elderId).phone("01012345678").build();
        ElderHealthInfo healthInfo = ElderHealthInfo.builder().build();
        List<Disease> diseases = Collections.emptyList();
        List<MedicationSchedule> medicationSchedules = Collections.emptyList();

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(elder));
        when(healthInfoRepository.findByElderId(elderId)).thenReturn(healthInfo);
        when(elderDiseaseRepository.findDiseasesByElder(elder)).thenReturn(diseases);
        when(medicationScheduleRepository.findByElderId(elderId)).thenReturn(medicationSchedules);
        
        when(callPromptGeneratorFactory.getGenerator(callType)).thenReturn(promptGenerator);
        when(promptGenerator.generate(elder, healthInfo, diseases, medicationSchedules)).thenReturn("생성된 프롬프트");

        // when
        careCallRequestSenderService.sendCall(settingId, elderId, callType);

        // then
        verify(careCallClient).requestCall(settingId, elderId, "01012345678", "생성된 프롬프트");
    }

    @Test
    @DisplayName("케어콜 발송 실패 - 어르신 없음")
    void sendCall_elderNotFound() {
        // given
        when(elderRepository.findById(1)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> careCallRequestSenderService.sendCall(10, 1, CallType.FIRST))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ELDER_NOT_FOUND);
    }
}
