package com.example.goorm_ticket.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.example.goorm_ticket.domain.coupon.entity.CouponIssuance;

@Getter
@Builder
public class CouponResponseDto {
    private Long id;
    private Long quantity;
    private String name;
    private Double discountRate;
    private LocalDateTime expirationDate;
    private CouponIssuance isSuccess;

    public static CouponResponseDto of(Long id, CouponIssuance isSuccess) {
        return CouponResponseDto.builder()
                .id(id)
                .isSuccess(isSuccess)
                .build();
    }

    public static CouponResponseDto of(Long id, String name) {
        return CouponResponseDto.builder()
                .id(id)
                .name(name)
                .build();
    }

    public static CouponResponseDto of(Long id, Long quantity, String name, Double discountRate, LocalDateTime expirationDate) {
        return CouponResponseDto.builder()
                .id(id)
                .quantity(quantity)
                .name(name)
                .discountRate(discountRate)
                .expirationDate(expirationDate)
                .build();
    }

}
