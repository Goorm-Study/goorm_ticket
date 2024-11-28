package com.example.goorm_ticket.domain.coupon.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CouponErrorResponseDto {
    private int status;
    private String errorCode;
    private String message;

    @Builder(access = AccessLevel.PRIVATE)
    private CouponErrorResponseDto(int status, String errorCode, String message) {
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
    }

    public static CouponErrorResponseDto of(int status, String errorCode, String message) {
        return CouponErrorResponseDto.builder()
                .status(status)
                .errorCode(errorCode)
                .message(message)
                .build();
    }

}
