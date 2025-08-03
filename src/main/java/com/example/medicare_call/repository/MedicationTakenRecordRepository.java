package com.example.medicare_call.repository;

import com.example.medicare_call.domain.MedicationTakenRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MedicationTakenRecordRepository extends JpaRepository<MedicationTakenRecord, Integer> {

    @Query("SELECT mtr FROM MedicationTakenRecord mtr " +
           "JOIN mtr.careCallRecord ccr " +
           "WHERE ccr.elder.id = :elderId " +
           "AND DATE(mtr.recordedAt) = :date")
    List<MedicationTakenRecord> findByElderIdAndDate(@Param("elderId") Integer elderId, @Param("date") LocalDate date);
} 