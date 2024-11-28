package com.example.goorm_ticket.domain.coupon.dto;

import lombok.*;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class CouponEventDto {
    @NotNull
    private Long userId;
    @NotNull
    private Long couponId;
    @NotNull
    private Long eventId;

    @Builder(access = AccessLevel.PRIVATE)
    private CouponEventDto(Long userId, Long couponId, Long eventId) {
        this.userId = userId;
        this.couponId = couponId;
        this.eventId = eventId;
    }

    public static CouponEventDto of(Long userId, Long couponId, Long eventId) {
        return CouponEventDto.builder()
                .userId(userId)
                .couponId(couponId)
                .eventId(eventId)
                .build();
    }
}
