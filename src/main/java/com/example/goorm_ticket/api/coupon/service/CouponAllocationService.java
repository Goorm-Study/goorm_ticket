package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.api.coupon.service.distribute.RedisLockStrategy;
import com.example.goorm_ticket.api.coupon.service.strategy.CouponAllocationStrategy;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
