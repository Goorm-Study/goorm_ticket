package com.example.goorm_ticket.api.coupon.exception;

import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import lombok.Getter;

@Getter
public class CouponException extends RuntimeException {
    private final String errorCode;

    public CouponException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }


    public CouponException(String message, Throwable e, String errorCode) {
        super(message, e);
        this.errorCode = errorCode;
    }

    public static class CouponQuantityShortageException extends CouponException {
        public CouponQuantityShortageException(Long remaining, Long quantity) {
            super("남은 쿠폰의 개수가 요청한 개수보다 적습니다. 남은 수량: " + remaining + ", 요청한 수량: " + quantity, "COUPON_SHORTAGE");
        }
    }

    public static class CouponNotFoundException extends CouponException {
        public CouponNotFoundException(Long couponId) {
            super("존재하지 않는 쿠폰입니다. 쿠폰 ID: " + couponId, "COUPON_NOT_FOUND");
        }
    }

    public static class UserNotFoundException extends CouponException {
        public UserNotFoundException(Long userId) {
            super("존재하지 않는 회원입니다. 회원 ID: " + userId, "USER_NOT_FOUND");
        }
    }



}
