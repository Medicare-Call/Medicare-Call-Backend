package com.example.medicare_call.service.carecall;

import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.global.enums.CallType;
import com.example.medicare_call.repository.CareCallSettingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CareCallSchedulerServiceTest {
    @Mock
    private CareCallSettingRepository careCallSettingRepository;

    @Mock
    private CareCallRequestSenderService callService;

    @InjectMocks
    private CareCallSchedulerService schedulerService;

    @Test
    void testCheckAndSendCalls_shouldCallEldersWithMatchingTimes() {
        // given
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);

        Elder dummyElder1 = Elder.builder().id(1).build();
        Elder dummyElder2 = Elder.builder().id(2).build();

        CareCallSetting firstSetting = CareCallSetting.builder()
                .elder(dummyElder1)
                .firstCallTime(now)
                .build();

        CareCallSetting secondSetting = CareCallSetting.builder()
                .elder(dummyElder2)
                .secondCallTime(now)
                .build();

        when(careCallSettingRepository.findByFirstCallTime(now)).thenReturn(List.of(firstSetting));
        when(careCallSettingRepository.findBySecondCallTime(now)).thenReturn(List.of(secondSetting));

        // when
        schedulerService.checkAndSendCalls();

        // then
        verify(callService).sendCall(firstSetting.getId(),1, CallType.FIRST);
        verify(callService).sendCall(secondSetting.getId(),2, CallType.SECOND);
    }
}
