package com.example.shared.api.coupon.service;

import com.example.shared.api.coupon.service.strategy.CouponAllocationStrategy;
import com.example.shared.domain.coupon.dto.CouponResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class CouponAllocationService {
    private final CouponAllocationStrategy allocationStrategy;

    public CouponAllocationService(CouponAllocationStrategy allocationStrategy) {
        this.allocationStrategy = allocationStrategy;
    }

    @Transactional
    public CouponResponseDto allocateCouponToUser(Long userId, Long couponId) {
        log.info("쿠폰 할당 - 사용자 ID: {}, 쿠폰 ID: {}, 전략: {}", userId, couponId, allocationStrategy.getClass().getSimpleName());
        return allocationStrategy.allocateCoupon(userId, couponId);
    }

//    private final RedisLockStrategy redisLockStrategy;
//
//    public CouponAllocationService(RedisLockStrategy redisLockStrategy) {
//        this.redisLockStrategy = redisLockStrategy;
//    }
//
//    @Transactional
//    public CouponResponseDto allocateCouponToUser(Long userId, Long couponId) {
//        log.info("쿠폰 할당 - 사용자 ID: {}, 쿠폰 ID: {}", userId, couponId);
//        return redisLockStrategy.allocateCoupon(userId, couponId);
//    }

}
