package com.example.medicare_call.service.report;

import com.example.medicare_call.dto.ElderHealthInfoCreateRequest;
import com.example.medicare_call.domain.*;
import com.example.medicare_call.dto.ElderHealthResponse;
import com.example.medicare_call.global.ResourceNotFoundException;
import com.example.medicare_call.global.enums.FrequencyType;
import com.example.medicare_call.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private final MemberRepository memberRepository;

    @Transactional
    public void registerElderHealthInfo(Integer elderId, ElderHealthInfoCreateRequest request) {
        // TODO : 이쪽 Exception에 대한 Monitoring 추가 필요. 데이터의 무결성이 깨졌을 확률이 높다
       Elder elder = elderRepository.findById(elderId)
               .orElseThrow(() -> new ResourceNotFoundException("어르신을 찾을 수 없습니다. elderId: " + elderId));

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
       for (ElderHealthInfoCreateRequest.MedicationScheduleRequest msReq : request.getMedicationSchedules()) {
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

    public List<ElderHealthResponse> getElderHealth(Integer memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("멤버를 찾을 수 없습니다. memberId + " +memberId));
        List<Elder> elders = elderRepository.findByGuardian(member);

        List<ElderHealthResponse> responses = new ArrayList<>();

        for(Elder elder : elders){

            // 질병 정보 추출
            List<String> diseases = elder.getElderDiseases().stream()
                    .map(elderDisease -> elderDisease.getDisease().getName())
                    .collect(Collectors.toList());

            // 약 복용 정보 추출
            Map<String, List<String>> medications = getMedicationsInfo(elder);

            // 특이사항 정보 추출
            List<String> specialNotes = new ArrayList<>();
            if (elder.getElderHealthInfo() != null && elder.getElderHealthInfo().getNotes() != null) {
                specialNotes = Arrays.stream(elder.getElderHealthInfo().getNotes().split("\\n"))
                        .map(String::trim)
                        .filter(note -> !note.isBlank())
                        .collect(Collectors.toList());
            }


            // 모든 정보를 ElderHealthResponse 객체로 조합
            ElderHealthResponse response = new ElderHealthResponse(
                    elder.getId(),
                    elder.getName(),
                    diseases,
                    medications,
                    specialNotes
            );

            responses.add(response);
        }

        return responses;


    }

    public Map<String, List<String>> getMedicationsInfo(Elder elder) {
        Map<String, List<String>> medications = new HashMap<>();

        // MedicationSchedule 값을 순회하며 Map 초기화
        for (MedicationSchedule time : elder.getMedicationSchedules()) {
            medications.put(time.getScheduleTime(), new ArrayList<>());
        }

        // Elder 엔티티의 medicationSchedules 리스트를 순회
        for (MedicationSchedule schedule : elder.getMedicationSchedules()) {
            String frequency = schedule.getScheduleTime();
            String medicationName = schedule.getMedication().getName();

            // 맵에 약 이름 추가
            if (medications.containsKey(frequency)) {
                medications.get(frequency).add(medicationName);
            }
        }
        return medications;
    }
} 