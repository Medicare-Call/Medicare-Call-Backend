package com.example.medicare_call.global.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
public abstract class HttpClient {

    private final RestTemplate restTemplate;

    protected <T> ResponseEntity<String> sendPostRequest(String url, HttpHeaders headers, T body) {
        try {
            HttpEntity<T> requestEntity = new HttpEntity<>(body, headers);
            return restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        } catch (Exception e) {
            log.error("HTTP 요청 실패 - URL: {}, Error: {}", url, e.getMessage());
            throw new RuntimeException("HTTP 요청 중 오류 발생", e);
        }
    }

    protected <T> ResponseEntity<String> sendGetRequest(String url, HttpHeaders headers) {
        try {
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            return restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        } catch (Exception e) {
            log.error("HTTP 요청 실패 - URL: {}, Error: {}", url, e.getMessage());
            throw new RuntimeException("HTTP 요청 중 오류 발생", e);
        }
    }
}
