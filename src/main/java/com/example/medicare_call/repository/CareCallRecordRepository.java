package com.example.medicare_call.repository;

import com.example.medicare_call.domain.CareCallRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareCallRecordRepository extends JpaRepository<CareCallRecord, Integer> {
} 