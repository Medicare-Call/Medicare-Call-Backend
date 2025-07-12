package com.example.medicare_call.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisTestController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/redis/test")
    public String testRedis() {
        try {
            // Redis에 테스트 데이터 저장
            redisTemplate.opsForValue().set("test-key", "Hello Redis!");

            // 저장된 데이터 조회
            String value = (String) redisTemplate.opsForValue().get("test-key");

            return "Redis 연결 성공! 저장된 값: " + value;
        } catch (Exception e) {
            return "Redis 연결 실패: " + e.getMessage();
        }
    }

    @GetMapping("/redis/ping")
    public String pingRedis() {
        try {
            String result = redisTemplate.getConnectionFactory()
                    .getConnection().ping();
            return "Redis Ping 성공: " + result;
        } catch (Exception e) {
            return "Redis Ping 실패: " + e.getMessage();
        }
    }
}

