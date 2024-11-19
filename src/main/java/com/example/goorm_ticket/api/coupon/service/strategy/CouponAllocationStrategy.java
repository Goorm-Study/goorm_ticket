package com.example.goorm_ticket.api.coupon.service.strategy;

import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;

public interface CouponAllocationStrategy {
    CouponResponseDto allocateCoupon(Long userId, Long couponId);
}