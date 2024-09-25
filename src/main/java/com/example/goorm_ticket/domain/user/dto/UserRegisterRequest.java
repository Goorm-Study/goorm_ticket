package com.example.goorm_ticket.domain.user.dto;

public record UserRegisterRequest(
        String username,
        String password
) {
}
