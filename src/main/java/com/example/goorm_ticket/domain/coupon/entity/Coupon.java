package com.example.goorm_ticket.domain.coupon.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double discountRate;

    @Column(nullable = false)
    private LocalDateTime expirationDate;

    @Builder
    private Coupon(Long quantity, String name, Double discountRate, LocalDateTime expirationDate) {
        this.quantity = quantity;
        this.name = name;
        this.discountRate = discountRate;
        this.expirationDate = expirationDate;
    }

    public void decreaseQuantity(Long quantity) {
        if(this.quantity < quantity) {
            throw new IllegalArgumentException("남은 쿠폰이 요청한 개수보다 적습니다.");
        }
        this.quantity -= quantity;
    }


}
