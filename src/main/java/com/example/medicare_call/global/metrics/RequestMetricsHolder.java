package com.example.medicare_call.global.metrics;

public class RequestMetricsHolder {
    private static final ThreadLocal<RequestMetricsContext> contextHolder = new ThreadLocal<>();

    public static void set(RequestMetricsContext context) { contextHolder.set(context); }
    public static RequestMetricsContext get() { return contextHolder.get(); }
    public static void clear() { contextHolder.remove(); }
}
