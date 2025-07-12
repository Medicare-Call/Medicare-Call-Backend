package com.example.medicare_call.service;

import com.example.medicare_call.dto.ElderHealthRegisterRequest;
import com.example.medicare_call.domain.*;
import com.example.medicare_call.repository.*;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ElderHealthInfoService {
    private final ElderRepository elderRepository;
    private final ElderHealthInfoRepository elderHealthInfoRepository;
    private final ElderDiseaseRepository elderDiseaseRepository;
    private final DiseaseRepository diseaseRepository;
    private final MedicationRepository medicationRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;

    @Transactional
    public void registerElderHealthInfo(Integer elderId, ElderHealthRegisterRequest request) {
        // TODO : 이쪽 Exception에 대한 Monitoring 추가 필요. 데이터의 무결성이 깨졌을 확률이 높다
       Elder elder = elderRepository.findById(elderId)
               .orElseThrow(() -> new IllegalArgumentException("어르신을 찾을 수 없습니다. elderId: " + elderId));

       // 질환 등록
       for (String diseaseName : request.getDiseaseNames()) {
           // 기획 변경: 사용자의 입력을 그대로 받는 방식으로 (데이터의 중복만 최소화하자)
           Disease disease = diseaseRepository.findByName(diseaseName)
                   .orElseGet(() -> diseaseRepository.save(Disease.builder().name(diseaseName).build()));

           ElderDisease elderDisease = ElderDisease.builder()
                   .elder(elder)
                   .disease(disease)
                   .build();
           elderDiseaseRepository.save(elderDisease);
       }

       // 복약 주기 등록
       for (ElderHealthRegisterRequest.MedicationScheduleRequest msReq : request.getMedicationSchedules()) {
           // 기획 변경: 사용자의 입력을 그대로 받는 방식으로 (데이터의 중복만 최소화하자)
           Medication medication = medicationRepository.findByName(msReq.getMedicationName())
                   .orElseGet(() -> medicationRepository.save(Medication.builder().name(msReq.getMedicationName()).build()));

           String scheduleTime = msReq.getScheduleTimes().stream()
                   .map(Enum::name)
                   .collect(Collectors.joining(","));

           MedicationSchedule schedule = MedicationSchedule.builder()
                   .elder(elder)
                   .medication(medication)
                   .scheduleTime(scheduleTime)
                   .build();
           medicationScheduleRepository.save(schedule);
       }

       // 특이사항 등록 (notes는 Enum 여러 개를 콤마로 join해서 저장)
       String notes = request.getNotes() != null ?
               request.getNotes().stream().map(Enum::name).collect(Collectors.joining(",")) : null;
       ElderHealthInfo healthInfo = ElderHealthInfo.builder()
               .elder(elder)
               .notes(notes)
               .build();
       elderHealthInfoRepository.save(healthInfo);
    }
} 