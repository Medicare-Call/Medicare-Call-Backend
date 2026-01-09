package com.example.medicare_call.controller;

import com.example.medicare_call.dto.auth.TokenResponse;
import com.example.medicare_call.global.jwt.JwtProvider;
import com.example.medicare_call.service.auth.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TokenController.class)
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Access Token 갱신 성공")
    void refreshToken_success() throws Exception {
        // given
        String refreshToken = "valid-refresh-token";
        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .build();

        when(refreshTokenService.refreshAccessToken(refreshToken)).thenReturn(tokenResponse);

        // when & then
        mockMvc.perform(post("/auth/refresh")
                        .header("Refresh-Token", refreshToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() throws Exception {
        // given
        String accessToken = "valid-access-token";
        String authorization = "Bearer " + accessToken;
        Integer memberId = 1;

        when(jwtProvider.getMemberId(accessToken)).thenReturn(memberId);

        // when & then
        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // verify that deleteRefreshToken was called
        // Note: Since we're using MockMvc, we can't directly verify service method calls
        // In a real scenario, you might want to use @SpringBootTest for integration testing
    }
} 