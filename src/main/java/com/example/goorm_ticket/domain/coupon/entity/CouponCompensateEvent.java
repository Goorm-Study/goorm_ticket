package com.example.goorm_ticket.domain.coupon.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class CouponCompensateEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long couponId;

    @Column(nullable = false)
    private Long quantity;

    @Builder(access = AccessLevel.PRIVATE)
    private CouponCompensateEvent(Long couponId, Long quantity) {
        this.couponId = couponId;
        this.quantity = quantity;
    }

    public static CouponCompensateEvent of(Long couponId, Long quantity) {
        return CouponCompensateEvent.builder()
                .couponId(couponId)
                .quantity(quantity)
                .build();
    }
}
