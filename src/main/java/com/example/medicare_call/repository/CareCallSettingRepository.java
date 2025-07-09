package com.example.medicare_call.repository;

import com.example.medicare_call.domain.CareCallSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareCallSettingRepository extends JpaRepository<CareCallSetting, Integer> {
} 