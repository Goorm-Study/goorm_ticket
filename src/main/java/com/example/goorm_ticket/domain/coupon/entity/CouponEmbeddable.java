package com.example.goorm_ticket.domain.coupon.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.Objects;

@Embeddable
@NoArgsConstructor
@Getter
public class CouponEmbeddable {
    private Long couponId;
    private String couponName;

    @Builder(access = AccessLevel.PRIVATE)
    private CouponEmbeddable(Long couponId, String couponName) {
        this.couponId = couponId;
        this.couponName = couponName;
    }

    public static CouponEmbeddable of(Long couponId, String couponName) {
        return CouponEmbeddable.builder()
                .couponId(couponId)
                .couponName(couponName)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CouponEmbeddable that = (CouponEmbeddable) o;
        return Objects.equals(couponId, that.couponId) && Objects.equals(couponName, that.couponName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(couponId, couponName);
    }
}
