package com.example.shared.domain.order.dto;

import lombok.Getter;

@Getter
public class OrderCancelDto {
    private Long userId;
    private Long seatId;
}
