package com.example.shared.api.coupon.service.strategy;


import com.example.shared.domain.coupon.dto.CouponResponseDto;

public interface CouponAllocationStrategy {
    CouponResponseDto allocateCoupon(Long userId, Long couponId);
}