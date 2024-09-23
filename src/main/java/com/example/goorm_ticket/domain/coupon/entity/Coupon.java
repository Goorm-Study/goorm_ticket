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

    @Builder(access = AccessLevel.PRIVATE)
    private Coupon(Long id, Long quantity, String name, Double discountRate, LocalDateTime expirationDate) {
        this.id = id;
        this.quantity = quantity;
        this.name = name;
        this.discountRate = discountRate;
        this.expirationDate = expirationDate;
    }

}
