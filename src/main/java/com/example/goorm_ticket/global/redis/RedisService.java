package com.example.goorm_ticket.global.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Slf4j
@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Lua 스크립트 파일 경로
    private static final String LUA_SCRIPT_PATH = "classpath:scripts/coupon_issue.lua";

    // Lua 스크립트 파일을 읽어오는 메서드
    private String loadLuaScript() throws IOException {
        File scriptFile = ResourceUtils.getFile(LUA_SCRIPT_PATH);
        return new String(Files.readAllBytes(scriptFile.toPath()));
    }

    // 쿠폰 발급 로직
    public String issueCoupon(String couponId, String userId) {
        try {
            String luaScript = loadLuaScript();

            String stockKey = "coupon:" + couponId + ":stock";
            String queueKey = "queue:" + couponId;

            // Lua 스크립트 실행
            Object result = redisTemplate.execute(
                    (connection) -> connection.eval(
                            luaScript.getBytes(),
                            ReturnType.VALUE,
                            2,
                            stockKey.getBytes(),
                            queueKey.getBytes(),
                            couponId.getBytes(),
                            userId.getBytes()
                    ),
                    false
            );
            log.info("Result from Lua script: {}", result);
            String resultString = new String((byte[]) result, StandardCharsets.UTF_8);
            log.info("Result from Lua script: {}", resultString);


            if ("Coupon issue".equals(resultString)) {
                return "Coupon issue";
            } else {
                throw new RuntimeException("Failed to issue coupon: " + result);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load Lua script", e);
        }
    }
}
