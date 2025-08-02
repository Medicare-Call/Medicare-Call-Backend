package com.example.medicare_call.repository;

import com.example.medicare_call.domain.CareCallRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface CareCallRecordRepository extends JpaRepository<CareCallRecord, Integer> {
    
    @Query("SELECT ccr FROM CareCallRecord ccr " +
           "WHERE ccr.elder.id = :elderId " +
           "AND DATE(ccr.startTime) = :date " +
           "AND ccr.sleepStart IS NOT NULL " +
           "ORDER BY ccr.startTime")
    List<CareCallRecord> findByElderIdAndDateWithSleepData(@Param("elderId") Integer elderId, @Param("date") LocalDate date);
} 