package com.example.shared.domain.user.dto;


import com.example.shared.domain.user.entity.User;

public record UserRegisterResponseDto(
    Long id,
    String username
) {
    public static UserRegisterResponseDto of(User user) {
        return new UserRegisterResponseDto(user.getId(), user.getUsername());
    }
}
