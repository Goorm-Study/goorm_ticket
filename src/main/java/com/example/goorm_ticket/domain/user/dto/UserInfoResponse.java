package com.example.goorm_ticket.domain.user.dto;

import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.user.entity.User;

import java.util.List;
import java.util.Map;

public record UserInfoResponse(
        Long id,
        String username,
        List<CouponEmbeddable> coupons
) {
    public static UserInfoResponse of(User user) {
        return new UserInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getCoupons()
        );
    }
}
