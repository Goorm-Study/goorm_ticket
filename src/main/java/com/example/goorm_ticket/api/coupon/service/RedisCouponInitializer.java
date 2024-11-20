package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import jakarta.annotation.PostConstruct;
import java.util.List;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisCouponInitializer {
    private final CouponRepository couponRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public RedisCouponInitializer(CouponRepository couponRepository, RedisTemplate<String, String> redisTemplate) {
        this.couponRepository = couponRepository;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void initializeCoupons() {
        List<Coupon> coupons = couponRepository.findAll();
        for (Coupon coupon : coupons) {
            String couponKey = "coupon:" + coupon.getId();
            redisTemplate.opsForValue().set(couponKey, "0"); // 발급 수량 초기화
        }
    }
}