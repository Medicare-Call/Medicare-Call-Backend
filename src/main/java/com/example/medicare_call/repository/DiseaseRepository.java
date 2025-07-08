package com.example.medicare_call.repository;

import com.example.medicare_call.domain.Disease;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiseaseRepository extends JpaRepository<Disease, Integer> {
} 