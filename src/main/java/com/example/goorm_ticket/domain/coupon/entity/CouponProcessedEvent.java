package com.example.goorm_ticket.domain.coupon.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor
public class CouponProcessedEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId;

    @Builder(access = AccessLevel.PRIVATE)
    private CouponProcessedEvent(Long eventId) {
        this.eventId = eventId;
    }

    public static CouponProcessedEvent of(Long eventId) {
        return CouponProcessedEvent.builder()
                .eventId(eventId)
                .build();
    }
}
