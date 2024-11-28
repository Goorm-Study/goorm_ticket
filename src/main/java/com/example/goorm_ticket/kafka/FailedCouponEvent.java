package com.example.goorm_ticket.kafka;

import lombok.Getter;

import java.util.HashMap;

@Getter
public class FailedCouponEvent {
    // key: couponId, value: quantity
    private final HashMap<Long, Long> couponQuantity = new HashMap<>();

    public void add(Long couponId) {
        couponQuantity.merge(couponId, 1L, Long::sum);
    }
}
