package com.example.goorm_ticket.domain.coupon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CouponResponseDto {
    private Long id;
    private Long quantity;
    private String name;
    private Double discountRate;
    private LocalDateTime expirationDate;

    public static CouponResponseDto of(Long id) {
        return CouponResponseDto.builder()
                .id(id)
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
