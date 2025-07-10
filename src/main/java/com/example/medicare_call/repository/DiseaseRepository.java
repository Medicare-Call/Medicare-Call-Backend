package com.example.medicare_call.repository;

import com.example.medicare_call.domain.Disease;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiseaseRepository extends JpaRepository<Disease, Integer> {
    Optional<Disease> findByName(String name);
} 