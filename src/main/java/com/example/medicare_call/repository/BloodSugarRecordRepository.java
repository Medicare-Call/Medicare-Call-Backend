package com.example.medicare_call.repository;

import com.example.medicare_call.domain.BloodSugarRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BloodSugarRecordRepository extends JpaRepository<BloodSugarRecord, Integer> {
} 