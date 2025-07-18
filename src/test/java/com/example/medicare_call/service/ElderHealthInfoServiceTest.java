package com.example.medicare_call.service;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.dto.ElderHealthRegisterRequest;
import com.example.medicare_call.global.enums.ElderHealthNoteType;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ElderHealthInfoServiceTest {
    @Mock ElderRepository elderRepository;
    @Mock ElderHealthInfoRepository elderHealthInfoRepository;
    @Mock ElderDiseaseRepository elderDiseaseRepository;
    @Mock DiseaseRepository diseaseRepository;
    @Mock MedicationRepository medicationRepository;
    @Mock MedicationScheduleRepository medicationScheduleRepository;
    @InjectMocks ElderHealthInfoService elderHealthInfoService;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void registerElderHealthInfo_success() {
        Elder elder = Elder.builder().id(1).build();
        Disease.builder().id(1).name("당뇨").build();
        Medication.builder().id(1).name("당뇨약").build();

        when(elderRepository.findById(1)).thenReturn(Optional.of(elder));
        when(diseaseRepository.findByName("당뇨")).thenReturn(Optional.empty());
        when(diseaseRepository.save(any(Disease.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(medicationRepository.findByName("당뇨약")).thenReturn(Optional.empty());
        when(medicationRepository.save(any(Medication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ElderHealthRegisterRequest.MedicationScheduleRequest msReq = ElderHealthRegisterRequest.MedicationScheduleRequest.builder()
                .medicationName("당뇨약")
                .scheduleTimes(List.of(MedicationScheduleTime.MORNING, MedicationScheduleTime.DINNER))
                .build();
        ElderHealthRegisterRequest request = ElderHealthRegisterRequest.builder()
                .diseaseNames(List.of("당뇨"))
                .medicationSchedules(List.of(msReq))
                .notes(List.of(ElderHealthNoteType.INSOMNIA))
                .build();

        elderHealthInfoService.registerElderHealthInfo(1, request);

        verify(elderDiseaseRepository, times(1)).save(any(ElderDisease.class));
        verify(medicationScheduleRepository, times(1)).save(any(MedicationSchedule.class));
        verify(elderHealthInfoRepository, times(1)).save(any(ElderHealthInfo.class));
    }
} 