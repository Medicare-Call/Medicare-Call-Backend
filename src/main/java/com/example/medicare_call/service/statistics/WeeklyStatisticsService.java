package com.example.medicare_call.service.statistics;

import com.example.medicare_call.domain.CareCallRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyStatisticsService {

    @Transactional
    public void updateWeeklyStatistics(CareCallRecord record) {
        /*
         * TODO: CareCallRecord 정보 기반, WeeklyStatistics Entity Upsert 진행
         * 기존의 DTO를 Entity로 변환하는 방식이 아닌, Entity 생성이 독립적으로 이뤄져야 함
         * 즉, 현재 존재하는 WeeklyReportService의 데이터 추출 및 AI 분석 형태는 착안하되, 내부의 DTO 등을 이곳의 로직에 사용해서는 절대 안됨
         * WeeklyReportService에 맞춘 추출이 아닌, WeeklyReportService Entity에 대응하는 데이터를 추출하도록 기존 로직의 일부 수정 필요
         * 추후 WeeklyReportService.getWeeklyReport() 에서 WeeklyStatistics 테이블 조회를 통해 WeeklyReportResponse를 구성하도록 수정 예정
         */
    }
}
