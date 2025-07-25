package com.example.medicare_call.repository;

import com.example.medicare_call.domain.CareCallSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface CareCallSettingRepository extends JpaRepository<CareCallSetting, Integer> {

    List<CareCallSetting> findByFirstCallTime(LocalTime now);

    List<CareCallSetting> findBySecondCallTime(LocalTime now);
}