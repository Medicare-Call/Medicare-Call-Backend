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
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(10, 10);


        Elder dummyElder1 = Elder.builder().id(1).build();
        Elder dummyElder2 = Elder.builder().id(2).build();

        CareCallSetting firstSetting = CareCallSetting.builder()
                .elder(dummyElder1)
                .firstCallTime(LocalTime.of(10, 5))
                .build();

        CareCallSetting secondSetting = CareCallSetting.builder()
                .elder(dummyElder2)
                .secondCallTime(LocalTime.of(10, 8))
                .build();

        when(careCallSettingRepository.findByFirstCallTimeBetween(startTime, endTime)).thenReturn(List.of(firstSetting));
        when(careCallSettingRepository.findBySecondCallTimeBetween(startTime, endTime)).thenReturn(List.of(secondSetting));

        // when
        schedulerService.checkAndSendCallsInRange(startTime, endTime);

        // then
        verify(callService).sendCall(firstSetting.getId(), dummyElder1.getId(), CallType.FIRST);
        verify(callService).sendCall(secondSetting.getId(), dummyElder2.getId(), CallType.SECOND);
    }
}
