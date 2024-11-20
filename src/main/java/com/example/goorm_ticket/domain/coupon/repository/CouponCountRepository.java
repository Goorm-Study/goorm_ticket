package com.example.goorm_ticket.domain.coupon.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CouponCountRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public CouponCountRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Long increment(String couponKey) {
        return redisTemplate
                .opsForValue()
                .increment("coupon_count");
    }

    public Long decrement(String couponKey) {
        return redisTemplate
                .opsForValue()
                .decrement("coupon_count");
    }

    public Long getIssuedCount(String couponKey) {
        String count = redisTemplate.opsForValue().get(couponKey);
        return Long.parseLong(count);
    }

}
