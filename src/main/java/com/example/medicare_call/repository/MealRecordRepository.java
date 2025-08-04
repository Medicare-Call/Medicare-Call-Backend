package com.example.medicare_call.repository;

import com.example.medicare_call.domain.MealRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MealRecordRepository extends JpaRepository<MealRecord, Integer> {
    
    @Query("SELECT mr FROM MealRecord mr " +
           "JOIN mr.careCallRecord ccr " +
           "WHERE ccr.elder.id = :elderId " +
           "AND DATE(mr.recordedAt) = :date " +
           "ORDER BY mr.recordedAt")
    List<MealRecord> findByElderIdAndDate(@Param("elderId") Integer elderId, @Param("date") LocalDate date);

    @Query("SELECT mr FROM MealRecord mr " +
           "JOIN mr.careCallRecord ccr " +
           "WHERE ccr.elder.id = :elderId " +
           "AND DATE(mr.recordedAt) BETWEEN :startDate AND :endDate " +
           "ORDER BY mr.recordedAt")
    List<MealRecord> findByElderIdAndDateBetween(@Param("elderId") Integer elderId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
} 