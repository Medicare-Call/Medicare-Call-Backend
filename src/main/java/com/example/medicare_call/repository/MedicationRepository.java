package com.example.medicare_call.repository;

import com.example.medicare_call.domain.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicationRepository extends JpaRepository<Medication, Integer> {
    Optional<Medication> findByName(String name);
} 