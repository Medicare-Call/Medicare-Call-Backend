package com.example.medicare_call.global.metrics;

public class RequestMetricsContext {
    private final String endpoint;
    private final String httpMethod;
    private long dbElapsedTimeNanos = 0;
    private int queryCount = 0;

    public RequestMetricsContext(String endpoint, String httpMethod) {
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
    }

    public void addDbTime(long nanos) {
        dbElapsedTimeNanos += nanos;
        queryCount++;
    }

    public long getDbElapsedTimeNanos() { return dbElapsedTimeNanos; }
    public int getQueryCount() { return queryCount; }
    public String getEndpoint() { return endpoint; }
    public String getHttpMethod() { return httpMethod; }
}
