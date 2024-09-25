package com.example.goorm_ticket.domain.user.dto;

import com.example.goorm_ticket.domain.user.entity.User;

public record UserRegisterResponse(
    Long id,
    String username
) {
    public static UserRegisterResponse of(User user) {
        return new UserRegisterResponse(user.getId(), user.getUsername());
    }
}
