package com.example.goorm_ticket.kafka;

import lombok.Getter;

import java.util.ArrayList;

@Getter
public class ProcessedCouponEvent {
    // key: couponId, value: userId
    ArrayList<Long> eventIdList = new ArrayList<>();

    public void add(Long eventId) {
        eventIdList.add(eventId);
    }
}
