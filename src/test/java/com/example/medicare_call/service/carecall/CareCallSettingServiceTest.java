package com.example.medicare_call.service.carecall;

import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.dto.carecall.CareCallSettingRequest;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.repository.CareCallSettingRepository;
import com.example.medicare_call.repository.ElderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CareCallSettingServiceTest {

    @Mock
    private CareCallSettingRepository careCallSettingRepository;
    @Mock
    private ElderRepository elderRepository;
    @InjectMocks
    private CareCallSettingService careCallSettingService;

    private Member createMember(Integer id) {
        return Member.builder().id(id).build();
    }

    private Elder createElder(Integer id, Member guardian) {
        return Elder.builder().id(id).guardian(guardian).build();
    }

    @Test
    @DisplayName("케어콜 설정 생성 성공")
    void createCareCallSetting_success() {
        // given
        Integer memberId = 1;
        Integer elderId = 1;
        Member member = createMember(memberId);
        Elder elder = createElder(elderId, member);
        CareCallSettingRequest request = new CareCallSettingRequest(LocalTime.of(9, 0), null, null);

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(elder));

        // when
        careCallSettingService.createCareCallSetting(memberId, elderId, request);

        // then
        verify(careCallSettingRepository).save(any(CareCallSetting.class));
    }
}


