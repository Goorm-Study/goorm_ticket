package com.example.goorm_ticket.domain.coupon.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CouponRequestDto {
    private Long userId;
    private Long couponId;

    @Builder(access = AccessLevel.PRIVATE)
    private CouponRequestDto(Long userId, Long couponId) {
        this.userId = userId;
        this.couponId = couponId;
    }

    public static CouponRequestDto of(Long userId, Long couponId) {
        return CouponRequestDto.builder()
                .userId(userId)
                .couponId(couponId)
                .build();
    }
}
