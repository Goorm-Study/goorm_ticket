package com.example.goorm_ticket.global.redis;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisInitializer implements CommandLineRunner {

    private final StringRedisTemplate redisTemplate;

    public RedisInitializer(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(String... args) {

        redisTemplate.delete("queue:1");
        redisTemplate.delete("queue:2");
        redisTemplate.getConnectionFactory().getConnection().ping();
        // 쿠폰 ID 및 재고 초기화
        redisTemplate.opsForValue().set("coupon:1:stock", "5");
        redisTemplate.opsForValue().set("coupon:2:stock", "15");
    }
}
