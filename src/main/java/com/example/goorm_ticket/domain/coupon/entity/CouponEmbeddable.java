package com.example.goorm_ticket.domain.coupon.entity;

import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.Objects;

@Embeddable
@NoArgsConstructor
@Getter
public class CouponEmbeddable {
    private Long id;
    private String name;

    @Builder(access = AccessLevel.PRIVATE)
    private CouponEmbeddable(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static CouponEmbeddable of(Long id, String name) {
        return CouponEmbeddable.builder()
                .id(id)
                .name(name)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CouponEmbeddable that = (CouponEmbeddable) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
