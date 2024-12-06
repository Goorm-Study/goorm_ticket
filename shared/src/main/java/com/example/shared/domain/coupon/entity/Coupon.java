package com.example.shared.domain.coupon.entity;

import com.example.shared.api.coupon.exception.CouponException.CouponQuantityShortageException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
