package com.example.goorm_ticket.domain.user.dto;

import com.example.goorm_ticket.domain.user.entity.User;

public record UserRegisterResponseDto(
    Long id,
    String username
) {
    public static UserRegisterResponseDto of(User user) {
        return new UserRegisterResponseDto(user.getId(), user.getUsername());
    }
}
