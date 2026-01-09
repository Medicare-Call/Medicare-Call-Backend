package com.example.medicare_call.util;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.global.enums.CallRecurrenceType;
import com.example.medicare_call.global.enums.ElderRelation;
import com.example.medicare_call.global.enums.Gender;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.global.enums.ResidenceType;
import com.example.medicare_call.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TestDataGenerator {

    private final ElderRepository elderRepository;
    private final MemberRepository memberRepository;
    private final CareCallSettingRepository careCallSettingRepository;
    private final CareCallRecordRepository careCallRecordRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;

    /**
     * 테스트용 Elder와 CareCallSetting을 조회하거나 생성합니다.
     */
    public CareCallRecord createOrGetTestCallRecord(Integer elderId, Integer settingId, String transcriptionText) {
        // 테스트용 Member 생성 또는 조회
        Member guardian = memberRepository.findById(1)
                .orElseGet(() -> {
                    Member newMember = Member.builder()
                            .id(1)
                            .name("테스트 보호자")
                            .phone("010-1234-5678")
                            .gender(Gender.MALE)
                            .termsAgreedAt(LocalDateTime.now())
                            .plan((byte) 1)
                            .build();
                    return memberRepository.save(newMember);
                });

        // Elder 조회 또는 생성
        Elder elder = elderRepository.findById(elderId)
                .orElseGet(() -> {
                    Elder newElder = Elder.builder()
                            .id(elderId)
//                            .guardian(guardian) # 2025 11/24 Many To Many 마이그레이션을 통해 일단 주석 처리
                            .name("테스트 어르신")
                            .gender(Gender.MALE.getCode())
                            .relationship(ElderRelation.CHILD)
                            .residenceType(ResidenceType.ALONE)
                            .build();
                    return elderRepository.save(newElder);
                });

        // CareCallSetting 조회 또는 생성
        CareCallSetting setting = careCallSettingRepository.findById(settingId)
                .orElseGet(() -> {
                    CareCallSetting newSetting = CareCallSetting.builder()
                            .id(settingId)
                            .elder(elder)
                            .firstCallTime(LocalDateTime.now().toLocalTime())
                            .recurrence(CallRecurrenceType.WEEKLY)
                            .build();
                    return careCallSettingRepository.save(newSetting);
                });

        // 테스트용 MedicationSchedule 생성 또는 조회
        MedicationSchedule schedule = medicationScheduleRepository.findByElder(elder)
                .stream()
                .filter(s -> s.getName().equals("혈압약"))
                .findFirst()
                .orElseGet(() -> {
                    MedicationSchedule newSchedule = MedicationSchedule.builder()
                            .name("혈압약")
                            .elder(elder)
                            .scheduleTime(MedicationScheduleTime.MORNING)
                            .build();
                    return medicationScheduleRepository.save(newSchedule);
                });

        // CareCallRecord 생성 및 저장
        CareCallRecord callRecord = CareCallRecord.builder()
                .elder(elder)
                .setting(setting)
                .calledAt(LocalDateTime.now())
                .responded((byte) 1)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusMinutes(15))
                .callStatus("completed")
                .transcriptionText(transcriptionText)
                .psychologicalDetails(null)
                .healthDetails(null)
                .build();

        return careCallRecordRepository.save(callRecord);
    }
}
