package com.example.medicare_call.global.config;

import com.example.medicare_call.global.metrics.RequestMetricsContext;
import com.example.medicare_call.global.metrics.RequestMetricsHolder;
import lombok.extern.slf4j.Slf4j;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;

import java.util.List;

@Slf4j
public class CustomQueryExecutionListener implements QueryExecutionListener {

    private static final long SLOW_QUERY_THRESHOLD_MS = 500;

    @Override
    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {

    }

    @Override
    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        RequestMetricsContext context = RequestMetricsHolder.get();
        if (context == null) return;

        int batchSize = execInfo.getBatchSize();
        int effectiveBatchSize = batchSize > 0 ? batchSize : 1;
        int executedQueries = queryInfoList.size() * effectiveBatchSize;

        long elapsedMs = execInfo.getElapsedTime();
        long elapsedNs = elapsedMs * 1_000_000;

        if (executedQueries == 1 && elapsedMs >= SLOW_QUERY_THRESHOLD_MS) {
            log.warn("[SLOW QUERY] {} ms - {}", elapsedMs, queryInfoList.get(0).getQuery());
        }

        context.addQueryMetrics(elapsedNs, executedQueries);
    }
}
