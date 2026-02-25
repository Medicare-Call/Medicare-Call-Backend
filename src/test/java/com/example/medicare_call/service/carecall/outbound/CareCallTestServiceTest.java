package com.example.medicare_call.service.carecall.outbound;

import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.dto.carecall.CareCallTestRequest;
import com.example.medicare_call.dto.carecall.ImmediateCareCallRequest.CareCallOption;
import com.example.medicare_call.global.enums.CallType;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.service.carecall.outbound.client.CareCallClient;
import com.example.medicare_call.service.carecall.setting.CareCallSettingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("케어콜 테스트 서비스 단위 테스트")
class CareCallTestServiceTest {

    @Mock
    private ElderRepository elderRepository;
    @Mock
    private CareCallSettingService careCallSettingService;
    @Mock
    private CareCallRequestSenderService careCallRequestSenderService;
    @Mock
    private CareCallClient careCallClient;

    @InjectMocks
    private CareCallTestService careCallTestService;

    @Test
    @DisplayName("즉시 케어콜 발송 성공")
    void sendImmediateCall_success() {
        // given
        Long elderId = 1L;
        CareCallOption option = CareCallOption.FIRST;
        Elder elder = Elder.builder().id(1).name("홍길동").build();
        CareCallSetting setting = CareCallSetting.builder().id(10).build();

        when(elderRepository.findById(1)).thenReturn(Optional.of(elder));
        when(careCallSettingService.getOrCreateImmediateSetting(elder)).thenReturn(setting);
        
        // when
        String result = careCallTestService.sendImmediateCall(elderId, option);

        // then
        verify(careCallRequestSenderService).sendCall(setting.getId(), 1, CallType.FIRST);
        assertThat(result).isEqualTo("홍길동 어르신께 즉시 케어콜 발송이 완료되었습니다.");
    }

    @Test
    @DisplayName("즉시 케어콜 발송 실패 - 어르신 없음")
    void sendImmediateCall_elderNotFound() {
        // given
        when(elderRepository.findById(1)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> careCallTestService.sendImmediateCall(1L, CareCallOption.FIRST))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ELDER_NOT_FOUND);
    }

    @Test
    @DisplayName("테스트 케어콜 발송 성공")
    void sendTestCall_success() {
        // given
        CareCallTestRequest request = new CareCallTestRequest("01012345678", "테스트");

        // when
        careCallTestService.sendTestCall(request);

        // then
        verify(careCallClient).requestCall(eq(100), eq(100), eq(request.phoneNumber()), eq("테스트"));
    }
}
