package com.example.medicare_call.service.carecall.outbound.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CareCallClient 단위 테스트")
class CareCallClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CareCallClient careCallClient;

    @Test
    @DisplayName("외부 케어콜 요청 성공")
    void requestCall_success() {
        // given
        String url = "http://test-care-call-url.com";
        ReflectionTestUtils.setField(careCallClient, "callUrl", url);
        
        when(restTemplate.exchange(eq(url), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("Success"));

        // when
        careCallClient.requestCall(1, 10, "010-1234-5678", "프롬프트");

        // then
        verify(restTemplate).exchange(eq(url), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }
}
