package com.example.goorm_ticket.domain.coupon.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Getter
public class CouponResponse {
    private Long id;
    private Long quantity;
    private String name;
    private Double discountRate;
    private LocalDateTime expirationDate;

    @Builder
    public CouponResponse(Long id, Long quantity, String name, Double discountRate, LocalDateTime expirationDate) {
        this.id = id;
        this.quantity = quantity;
        this.name = name;
        this.discountRate = discountRate;
        this.expirationDate = expirationDate;
    }
}
