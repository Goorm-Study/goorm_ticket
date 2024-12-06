package com.example.shared.domain.user.dto;

public record UserRegisterRequestDto(
        String username,
        String password
) {
}
