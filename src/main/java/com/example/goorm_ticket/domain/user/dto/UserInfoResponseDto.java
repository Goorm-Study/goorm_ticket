package com.example.goorm_ticket.domain.user.dto;

import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.user.entity.User;

import java.util.List;
import java.util.Map;

public record UserInfoResponseDto(
        Long id,
        String username,
        List<CouponEmbeddable> coupons
) {
    public static UserInfoResponseDto of(User user) {
        return new UserInfoResponseDto(
                user.getId(),
                user.getUsername(),
                user.getCoupons()
        );
    }
}
