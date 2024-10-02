package com.example.goorm_ticket.domain.order.dto;

import lombok.Getter;

@Getter
public class OrderCancelDto {
    private Long userId;
    private Long seatId;
}
