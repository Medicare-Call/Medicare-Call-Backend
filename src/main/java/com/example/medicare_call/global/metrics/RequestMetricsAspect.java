package com.example.medicare_call.global.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RequestMetricsAspect {

    private final MeterRegistry meterRegistry;

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object aroundController(ProceedingJoinPoint pjp) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return pjp.proceed();

        HttpServletRequest request = attributes.getRequest();
        String endpoint = extractEndpoint(request);
        String method = request.getMethod();

        RequestMetricsHolder.set(new RequestMetricsContext(endpoint, method));

        try {
            return pjp.proceed();
        } finally {
            recordMetrics();
            RequestMetricsHolder.clear();
        }
    }

    private String extractEndpoint(HttpServletRequest request) {
        Object bestPattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return bestPattern != null ? bestPattern.toString() : request.getRequestURI();
    }

    private void recordMetrics() {
        RequestMetricsContext context = RequestMetricsHolder.get();
        if (context == null) return;

        long nanos = context.getDbElapsedTimeNanos();
        int totalQueries = context.getQueryCount();

        Timer.builder("http.db.io.time")
                .tag("uri", context.getEndpoint())
                .tag("method", context.getHttpMethod())
                .description("Database I/O time per HTTP request")
                .register(meterRegistry)
                .record(nanos, TimeUnit.NANOSECONDS);

        Counter.builder("http.db.query.count")
                .tag("uri", context.getEndpoint())
                .tag("method", context.getHttpMethod())
                .description("Number of database queries per HTTP request")
                .register(meterRegistry)
                .increment(totalQueries);

        log.debug("Recorded metrics - {} {} - {}ms, {} queries",
                context.getHttpMethod(),
                context.getEndpoint(),
                nanos / 1_000_000.0,
                totalQueries);
    }
}
