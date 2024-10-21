package com.example.goorm_ticket.domain.order.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderCreateDto {
    private Long userId;
    private Long seatId;
    private Long couponId;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderCreateDto(Long userId, Long seatId, Long couponId) {
        this.userId = userId;
        this.seatId = seatId;
        this.couponId = couponId;
    }

    public static OrderCreateDto of(Long userId, Long seatId, Long couponId) {
        return OrderCreateDto.builder()
                .userId(userId)
                .seatId(seatId)
                .couponId(couponId)
                .build();
    }
}
