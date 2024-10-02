package com.example.goorm_ticket.domain.order.dto;

import lombok.Getter;

@Getter
public class OrderCreateDto {
    private Long userId;
    private Long seatId;
    private Long couponId;
}
