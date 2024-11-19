package com.example.goorm_ticket.domain.coupon.entity;

import com.example.goorm_ticket.api.coupon.exception.CouponException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.example.goorm_ticket.api.coupon.exception.CouponException.*;

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

//    @Version // 낙관적 락을 위한 버전 필드 추가
//    private Integer version;

    @Builder(access = AccessLevel.PRIVATE)
    private Coupon(Long quantity, String name, Double discountRate, LocalDateTime expirationDate) {
        this.quantity = quantity;
        this.name = name;
        this.discountRate = discountRate;
        this.expirationDate = expirationDate;
    }

    public static Coupon of(Long quantity, String name, Double discountRate, LocalDateTime expirationDate) {
        return Coupon.builder()
                .quantity(quantity)
                .name(name)
                .discountRate(discountRate)
                .expirationDate(expirationDate)
                .build();
    }

    public synchronized void decreaseQuantity(Long quantity) {
        if(this.quantity < quantity) {
            throw new CouponQuantityShortageException(this.quantity, quantity);
        }
        this.quantity -= quantity;
    }

}
