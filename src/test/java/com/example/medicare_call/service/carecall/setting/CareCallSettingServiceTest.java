package com.example.medicare_call.service.carecall.setting;

import com.example.medicare_call.domain.CareCallSetting;
import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.Member;
import com.example.medicare_call.domain.MemberElder;
import com.example.medicare_call.dto.carecall.CareCallSettingRequest;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import com.example.medicare_call.global.enums.MemberElderAuthority;
import com.example.medicare_call.repository.CareCallSettingRepository;
import com.example.medicare_call.repository.ElderRepository;
import com.example.medicare_call.repository.MemberElderRepository;
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
    @Mock
    private MemberElderRepository memberElderRepository;
    @InjectMocks
    private CareCallSettingService careCallSettingService;

    private Member createMember(Integer id) {
        return Member.builder().id(id).build();
    }

    private Elder createElder(Integer id) {
        return Elder.builder().id(id).build();
    }

    private MemberElder createRelation(Member member, Elder elder) {
        return MemberElder.builder()
                .guardian(member)
                .elder(elder)
                .authority(MemberElderAuthority.MANAGE)
                .build();
    }

    @Test
    @DisplayName("케어콜 설정 생성 성공")
    void upsertCareCallSetting_create_success() {
        // given
        Integer memberId = 1;
        Integer elderId = 1;
        Member member = createMember(memberId);
        Elder elder = createElder(elderId);
        CareCallSettingRequest request = new CareCallSettingRequest(LocalTime.of(9, 0), null, null);

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(elder));
        when(memberElderRepository.findByGuardian_IdAndElder_Id(memberId, elderId))
                .thenReturn(Optional.of(createRelation(member, elder)));
        when(careCallSettingRepository.findByElder(elder)).thenReturn(Optional.empty());

        // when
        careCallSettingService.upsertCareCallSetting(memberId, elderId, request);

        // then
        verify(careCallSettingRepository).save(any(CareCallSetting.class));
    }

    @Test
    @DisplayName("케어콜 설정 수정 성공")
    void upsertCareCallSetting_update_success() {
        // given
        Integer memberId = 1;
        Integer elderId = 1;
        Member member = createMember(memberId);
        Elder elder = createElder(elderId);
        CareCallSettingRequest request = new CareCallSettingRequest(LocalTime.of(10, 0), LocalTime.of(15, 0), null);
        CareCallSetting existingSetting = CareCallSetting.builder()
                .elder(elder)
                .firstCallTime(LocalTime.of(9, 0))
                .build();

        when(elderRepository.findById(elderId)).thenReturn(Optional.of(elder));
        when(memberElderRepository.findByGuardian_IdAndElder_Id(memberId, elderId))
                .thenReturn(Optional.of(createRelation(member, elder)));
        when(careCallSettingRepository.findByElder(elder)).thenReturn(Optional.of(existingSetting));

        // when
        careCallSettingService.upsertCareCallSetting(memberId, elderId, request);

        // then
        assertEquals(LocalTime.of(10, 0), existingSetting.getFirstCallTime());
        assertEquals(LocalTime.of(15, 0), existingSetting.getSecondCallTime());
        assertNull(existingSetting.getThirdCallTime());
        verify(careCallSettingRepository, never()).save(any(CareCallSetting.class));
    }
}
