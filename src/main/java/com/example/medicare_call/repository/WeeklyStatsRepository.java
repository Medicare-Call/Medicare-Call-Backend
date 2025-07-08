package com.example.medicare_call.repository;

import com.example.medicare_call.domain.WeeklyStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyStatsRepository extends JpaRepository<WeeklyStats, Integer> {
} 