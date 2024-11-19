package com.example.goorm_ticket.domain.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OrderCreateDto {
    private Long userId;
    private Long seatId;
    private Long couponId;
}
