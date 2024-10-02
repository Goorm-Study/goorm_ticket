package com.example.goorm_ticket.domain.user.dto;

import com.example.goorm_ticket.domain.user.entity.User;

import java.util.Map;

public record UserInfoResponseDto(
        Long id,
        String username,
        Map<Long, String> coupons
) {
    public static UserInfoResponseDto of(User user) {
        return new UserInfoResponseDto(
                user.getId(),
                user.getUsername(),
                user.getCoupons()
        );
    }
}
