package com.example.goorm_ticket.domain.order.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OrderResponseDto {
    private Long seatId;
}
