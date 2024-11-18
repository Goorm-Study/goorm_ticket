package com.example.goorm_ticket.api.coupon.exception;

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

    public static class CouponDuplicateAllocateException extends CouponException {
        public CouponDuplicateAllocateException(Long userId, Long couponId) {
            super("user:" + userId + "가 이미 쿠폰: " + couponId + "을 발급 요청하였습니다.", "COUPON_DUPLICATE_ALLOCATE");
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

    public static class CouponEventSerializationException extends CouponException {
        public CouponEventSerializationException() {
            super("쿠폰 발행 이벤트를 직렬화하는데 실패했습니다.", "COUPON_EVENT_SERIALIZATION_FAILED");
        }
    }

    public static class CouponEventDeserializationException extends CouponException {
        public CouponEventDeserializationException() {
            super("쿠폰 발행 이벤트를 역직렬화하는데 실패했습니다.", "COUPON_EVENT_DESERIALIZATION_FAILED");
        }
    }

}
