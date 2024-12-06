package com.example.shared.api.coupon.service.strategy;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RedisQueueMaintenance {

    private final RedisTemplate<String, Long> redisTemplate;

    public RedisQueueMaintenance(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void cleanUpExpiredQueues() {
        // 특정 쿠폰 ID별 대기열 확인 및 만료된 요청 제거 로직
    }
}