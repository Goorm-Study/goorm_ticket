package com.example.goorm_ticket.domain.user.dto;

public record UserRegisterRequestDto(
        String username,
        String password
) {
}
