package com.example.shared.domain.user.dto;

import com.example.shared.domain.coupon.entity.UserCoupon;
import com.example.shared.domain.user.entity.User;
import java.util.List;

public record UserInfoResponseDto(
        Long id,
        String username,
        List<UserCoupon> coupons
) {
    public static UserInfoResponseDto of(User user) {
        return new UserInfoResponseDto(
                user.getId(),
                user.getUsername(),
                user.getUserCoupons()
        );
    }
}
