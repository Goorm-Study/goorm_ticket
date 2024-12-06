package com.example.shared.domain.coupon.repository;

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
                .increment(couponKey);
    }

    public Long decrement(String couponKey) {
        return redisTemplate
                .opsForValue()
                .decrement(couponKey);
    }

    public Long getIssuedCount(String couponKey) {
        String count = redisTemplate.opsForValue().get(couponKey);
        if (count == null) {
            return 0L;
        }
        return Long.parseLong(count);
    }

    //쿠폰 초기 수량 관리
    public void setCouponQuantity(Long couponId, Long quantity) {
        String key = "coupon:quantity:" + couponId;
        redisTemplate.opsForValue().set(key, String.valueOf(quantity));
    }

    public Long getCouponQuantity(Long couponId) {
        String key = "coupon:quantity:" + couponId;
        String quantity = redisTemplate.opsForValue().get(key);
        if (quantity == null) {
            throw new RuntimeException("쿠폰 수량 정보가 없습니다: " + couponId);
        }
        return Long.parseLong(quantity);
    }

}
