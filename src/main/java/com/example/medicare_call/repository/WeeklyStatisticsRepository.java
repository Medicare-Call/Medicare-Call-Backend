package com.example.medicare_call.repository;

import com.example.medicare_call.domain.Elder;
import com.example.medicare_call.domain.WeeklyStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface WeeklyStatisticsRepository extends JpaRepository<WeeklyStatistics, Long> {
    Optional<WeeklyStatistics> findByElderAndStartDate(Elder elder, LocalDate startDate);
}
