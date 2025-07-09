package com.example.medicare_call.global.jwt;

import com.example.medicare_call.domain.Member;
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

    private final SecretKey secretKey;

    public JwtProvider(@Value("${jwt.secret}") String secretKey,
                       @Value("${jwt.accessTokenExpiration}") Long accessTokenExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.ACCESS_TOKEN_EXPIRE_MILLIS = accessTokenExpiration;
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

    public boolean verify(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
        } catch (SecurityException | MalformedJwtException e) {
            log.debug("잘못된 Jwt 서명입니다.");
            throw new IllegalArgumentException("JWT 검증 중 오류가 발생했습니다.");
        } catch (ExpiredJwtException e) {
            log.debug("만료된 토큰입니다.");
            throw new IllegalArgumentException("JWT 검증 중 오류가 발생했습니다.");
        } catch (UnsupportedJwtException e) {
            log.debug("지원하지 않는 토큰입니다.");
            throw new IllegalArgumentException("JWT 검증 중 오류가 발생했습니다.");
        } catch (IllegalArgumentException e) {
            log.debug("잘못된 토큰입니다.");
            throw new IllegalArgumentException("JWT 검증 중 오류가 발생했습니다.");
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw new IllegalArgumentException("JWT 검증 중 오류가 발생했습니다.");
        }

        return true;
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getUserId(String token) {
        return Long.parseLong(getClaims(token).get("userId").toString());
    }

}
