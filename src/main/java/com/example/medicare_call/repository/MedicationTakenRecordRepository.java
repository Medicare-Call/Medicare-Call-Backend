package com.example.medicare_call.repository;

import com.example.medicare_call.domain.MedicationTakenRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationTakenRecordRepository extends JpaRepository<MedicationTakenRecord, Integer> {
} 