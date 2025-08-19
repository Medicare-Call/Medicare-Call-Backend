package com.example.medicare_call.service.report;

import com.example.medicare_call.domain.*;
import com.example.medicare_call.dto.ElderHealthInfoCreateRequest;
import com.example.medicare_call.dto.ElderHealthInfoResponse;
import com.example.medicare_call.global.enums.MedicationScheduleTime;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
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
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void upsertElderHealthInfo(Integer elderId, ElderHealthInfoCreateRequest request) {
        // TODO : 이쪽 Exception에 대한 Monitoring 추가 필요. 데이터의 무결성이 깨졌을 확률이 높다
        Elder elder = elderRepository.findById(elderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ELDER_NOT_FOUND));

        // 질환 정보 업데이트
        if (request.getDiseaseNames() != null) {
            elderDiseaseRepository.deleteAllByElder(elder);
            Set<String> diseaseNames = request.getDiseaseNames().stream()
                    .flatMap(name -> Arrays.stream(name.split(",\\s*")))
                    .map(String::trim)
                    .collect(Collectors.toSet());

            for (String diseaseName : diseaseNames) {
                Disease disease = diseaseRepository.findByName(diseaseName)
                        .orElseGet(() -> {
                            Disease newDisease = Disease.builder().name(diseaseName).build();
                            return diseaseRepository.save(newDisease);
                        });

                ElderDisease elderDisease = ElderDisease.builder()
                        .elder(elder)
                        .disease(disease)
                        .build();
                elderDiseaseRepository.save(elderDisease);
            }
        }

        // 복약 주기 등록
        if (request.getMedicationSchedules() != null && !request.getMedicationSchedules().isEmpty()) {
            medicationScheduleRepository.deleteAllByElder(elder);
            for (ElderHealthInfoCreateRequest.MedicationScheduleRequest msReq : request.getMedicationSchedules()) {
                for (String scheduleTimeStr : msReq.getScheduleTimes()) {
                    String[] times = scheduleTimeStr.split(",\\s*");
                    for (String time : times) {
                        MedicationScheduleTime scheduleTime = MedicationScheduleTime.valueOf(time.trim().toUpperCase());
                        MedicationSchedule schedule = MedicationSchedule.builder()
                                .elder(elder)
                                .name(msReq.getMedicationName())
                                .scheduleTime(scheduleTime)
                                .build();
                        medicationScheduleRepository.save(schedule);
                    }
                }
            }
        }

        // 특이사항 등록 (notes는 Enum 여러 개를 콤마로 join해서 저장)
        String notes = request.getNotes() != null && !request.getNotes().isEmpty() ?
                request.getNotes().stream().map(Enum::name).collect(Collectors.joining(",")) : null;

        if (notes != null) {
            elderHealthInfoRepository.findByElder(elder)
                    .ifPresentOrElse(
                            existingHealthInfo -> {
                                // 기존 데이터가 있으면 업데이트
                                existingHealthInfo.setNotes(notes);
                                elderHealthInfoRepository.save(existingHealthInfo);
                            },
                            () -> {
                                // 기존 데이터가 없으면 새로 생성
                                ElderHealthInfo healthInfo = ElderHealthInfo.builder()
                                        .elder(elder)
                                        .notes(notes)
                                        .build();
                                elderHealthInfoRepository.save(healthInfo);
                            }
                    );
        } else {
            // 요청에 특이사항이 없으면 기존 데이터 삭제
            elderHealthInfoRepository.deleteAllByElder(elder);
        }
    }
    public List<ElderHealthInfoResponse> getElderHealth(Integer memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        List<Elder> elders = elderRepository.findByGuardian(member);

        List<ElderHealthInfoResponse> responses = new ArrayList<>();

        for(Elder elder : elders){

            // 질병 정보 추출
            List<String> diseases = elder.getElderDiseases().stream()
                    .map(elderDisease -> elderDisease.getDisease().getName())
                    .collect(Collectors.toList());

            // 약 복용 정보 추출
            Map<String, List<String>> medications = getMedicationsInfo(elder);

            // 특이사항 정보 추출
            List<String> notes = new ArrayList<>();
            if (elder.getElderHealthInfo() != null && elder.getElderHealthInfo().getNotes() != null) {
                notes = Arrays.stream(elder.getElderHealthInfo().getNotes().split(","))
                        .map(String::trim)
                        .filter(note -> !note.isBlank())
                        .collect(Collectors.toList());
            }


            // 모든 정보를 ElderHealthResponse 객체로 조합
            ElderHealthInfoResponse response = new ElderHealthInfoResponse(
                    elder.getId(),
                    elder.getName(),
                    diseases,
                    medications,
                    notes
            );

            responses.add(response);
        }

        return responses;
    }

    public Map<String, List<String>> getMedicationsInfo(Elder elder) {
        Map<String, List<String>> medications = new HashMap<>();

        for (MedicationSchedule schedule : elder.getMedicationSchedules()) {
            String scheduleTime = schedule.getScheduleTime().name();
            String medicationName = schedule.getName();

            String[] times = scheduleTime.split(",");
            for (String time : times) {
                String trimmedTime = time.trim();
                medications.computeIfAbsent(trimmedTime, k -> new ArrayList<>()).add(medicationName);
            }
        }
        return medications;
    }
} 