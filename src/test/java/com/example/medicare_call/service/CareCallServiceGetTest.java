package com.example.medicare_call.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CareCallServiceGetTest {

    @Autowired
    private CareCallService careCallService;

    @Test
    @Disabled("실제 서버 요청이므로 일시적으로 비활성화. 실행될 때마다 전화가 계속 걸림")
    @DisplayName("전화 서버 통합 테스트")
    void testRealServerCall() {
        // When - 실제 Spring Context의 서비스 사용
        String result = careCallService.testGetRequest();

        // Then
        assertTrue(result.contains("GET 요청 성공"));
        System.out.println("실제 통합 테스트 결과: " + result);
    }
}
