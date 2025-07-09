package com.example.medicare_call.repository;

import com.example.medicare_call.domain.MedicationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationScheduleRepository extends JpaRepository<MedicationSchedule, Integer> {
} 