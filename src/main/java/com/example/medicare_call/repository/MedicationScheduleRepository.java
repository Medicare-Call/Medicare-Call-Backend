package com.example.medicare_call.repository;

import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.domain.Elder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicationScheduleRepository extends JpaRepository<MedicationSchedule, Integer> {
    List<MedicationSchedule> findByElderId(Integer elderId);
    List<MedicationSchedule> findByElder(Elder elder);
}
