package com.example.medicare_call.repository;

import com.example.medicare_call.domain.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationRepository extends JpaRepository<Medication, Integer> {
} 