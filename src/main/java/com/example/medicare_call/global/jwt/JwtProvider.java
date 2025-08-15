package com.example.medicare_call.global.jwt;

import com.example.medicare_call.domain.Member;
import com.example.medicare_call.global.exception.CustomException;
import com.example.medicare_call.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    private final Long ACCESS_TOKEN_EXPIRE_MILLIS;
    private final Long PHONE_TOKEN_EXPIRE_MILLIS;
    private final SecretKey secretKey;

    public JwtProvider(@Value("${jwt.secret}") String secretKey,
                       @Value("${jwt.accessTokenExpiration}") Long accessTokenExpiration,
                       @Value("${jwt.phoneTokenExpiration}") Long phoneTokenExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.ACCESS_TOKEN_EXPIRE_MILLIS = accessTokenExpiration;
        this.PHONE_TOKEN_EXPIRE_MILLIS = phoneTokenExpiration;
    }

    public String createAccessToken(Member member) {
        Date now = new Date();
        return Jwts.builder()
                .claim("category", "access")
                .claim("id", member.getId())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_MILLIS))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createPhoneVerificationToken(String phoneNumber) {
        Date now = new Date();
        return Jwts.builder()
                .claim("category", "phone")
                .claim("phone", phoneNumber)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + PHONE_TOKEN_EXPIRE_MILLIS))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean verify(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("JWT 토큰 만료 - Token: [{}]", token);
            throw new CustomException(ErrorCode.ACCESS_TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            log.error("유효하지 않은 JWT 토큰 - Token: [{}], Error: {}", token, e.getMessage());
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getMemberId(String token) {
        return Long.parseLong(getClaims(token).get("id").toString());
    }

    public String getPhone(String token) {
        return getClaims(token).get("phone").toString();
    }

    public String getTokenCategory(String token) {
        return getClaims(token).get("category").toString();
    }

    public boolean isAccessToken(String token) {
        try {
            return "access".equals(getTokenCategory(token));
        } catch (Exception e) {
            return false;
        }
    }

    public Long getAccessTokenExpirationMillis() {
        return ACCESS_TOKEN_EXPIRE_MILLIS;
    }

}
