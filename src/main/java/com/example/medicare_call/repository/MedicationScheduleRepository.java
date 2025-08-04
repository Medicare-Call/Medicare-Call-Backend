package com.example.medicare_call.repository;

import com.example.medicare_call.domain.MedicationSchedule;
import com.example.medicare_call.domain.Elder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MedicationScheduleRepository extends JpaRepository<MedicationSchedule, Integer> {
    List<MedicationSchedule> findByElder(Elder elder);
    
    @Query("SELECT ms FROM MedicationSchedule ms " +
           "WHERE ms.elder.id = :elderId")
    List<MedicationSchedule> findByElderId(@Param("elderId") Integer elderId);
} 