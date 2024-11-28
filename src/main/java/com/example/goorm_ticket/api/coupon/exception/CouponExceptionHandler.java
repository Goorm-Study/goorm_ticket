package com.example.goorm_ticket.api.coupon.exception;

import com.example.goorm_ticket.domain.coupon.dto.CouponErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CouponExceptionHandler {
    @ExceptionHandler(CouponException.class)
    public ResponseEntity<CouponErrorResponseDto> handleCouponException(CouponException e) {
        CouponErrorResponseDto errorResponseDto = CouponErrorResponseDto.of(
                HttpStatus.BAD_REQUEST.value(),
                e.getErrorCode(),
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponseDto);
    }
}
