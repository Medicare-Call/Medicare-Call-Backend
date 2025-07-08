package com.example.medicare_call.repository;

import com.example.medicare_call.domain.MealRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealRecordRepository extends JpaRepository<MealRecord, Integer> {
} 