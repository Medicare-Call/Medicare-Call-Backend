package com.example.medicare_call.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "*");
        response.setHeader("Access-Control-Allow-Headers", "*");

        String token = getToken(request);
        if (token != null && jwtProvider.verify(token)) {
            Authentication authentication = getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String getToken(HttpServletRequest request) {
        String authorization = request.getHeader("authorization");
        String validTokenPrefix = "Bearer ";

        if (authorization == null || !authorization.startsWith(validTokenPrefix)) {
            return null;
        }
        return authorization.substring(validTokenPrefix.length()).trim();
    }

    private Authentication getAuthentication(String token) {
        try {
            // Access Token인지 확인
            if (jwtProvider.isAccessToken(token)) {
                Long memberId = jwtProvider.getMemberId(token);
                log.info("Extracted memberId from Access Token: {}", memberId);
                return new JwtTokenAuthentication(memberId);
            } else {
                // Phone Token인 경우
                String phone = jwtProvider.getPhone(token);
                log.info("Extracted phone from Phone Token: {}", phone);
                return new JwtPhoneTokenAuthentication(phone);
            }
        } catch (Exception e) {
            log.error("토큰 인증 처리 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }
}
