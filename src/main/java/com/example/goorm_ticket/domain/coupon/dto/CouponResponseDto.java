package com.example.goorm_ticket.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Getter
@Builder
public class CouponResponseDto {
    private Long id;
    private Long quantity;
    private String name;
    private Double discountRate;
    private LocalDateTime expirationDate;
    private String message;

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

    //테스트용 - 메시지 받기
    public static CouponResponseDto of(Long id, String name, Long quantity,  String message) {
        return CouponResponseDto.builder()
                .id(id)
                .name(name)
                .quantity(quantity)
                .message(message)
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
