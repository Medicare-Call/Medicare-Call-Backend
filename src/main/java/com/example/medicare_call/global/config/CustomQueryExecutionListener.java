package com.example.medicare_call.global.config;

import com.example.medicare_call.global.metrics.RequestMetricsContext;
import com.example.medicare_call.global.metrics.RequestMetricsHolder;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;

import java.util.List;

public class CustomQueryExecutionListener implements QueryExecutionListener {

    @Override
    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        System.out.println("BEFORE");
        execInfo.addCustomValue("startTime", System.nanoTime());
    }

    @Override
    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        System.out.println("AFTER");
        RequestMetricsContext context = RequestMetricsHolder.get();
        if (context == null) return;

        Long start = execInfo.getCustomValue("startTime", Long.class);
        long elapsed;
        if (start != null) {
            elapsed = System.nanoTime() - start;
        } else {
            elapsed = execInfo.getElapsedTime() * 1_000_000;
        }
        context.addDbTime(elapsed);
    }
}
