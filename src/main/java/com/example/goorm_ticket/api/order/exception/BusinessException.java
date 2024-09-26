package com.example.goorm_ticket.api.order.exception;

import com.example.goorm_ticket.domain.event.entity.SeatStatus;
import com.example.goorm_ticket.domain.order.entity.OrderStatus;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final String errorCode;

    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 좌석을 찾을 수 없는 경우
     */
    public static class SeatNotFoundException extends BusinessException {
        public SeatNotFoundException(Long seatId) {
            super("좌석을 찾을 수 없습니다. 좌석 ID: " + seatId, "SEAT_NOT_FOUND");
        }
    }

    /**
     * 주문을 찾을 수 없는 경우
     */
    public static class OrderNotFoundException extends BusinessException {
        public OrderNotFoundException(Long seatId) {
            super("해당 좌석에 연결된 주문이 없습니다. 좌석 ID: " + seatId, "ORDER_NOT_FOUND");
        }
    }

    /**
     * 이벤트 ID 불일치 경우
     */
    public static class EventMismatchException extends BusinessException {
        public EventMismatchException(Long eventId, Long seatEventId) {
            super("이벤트 ID가 일치하지 않습니다. 요청된 이벤트 ID: " + eventId + ", 좌석의 이벤트 ID: " + seatEventId, "EVENT_MISMATCH");
        }
    }

    /**
     * 쿠폰을 찾을 수 없는 경우
     */
    public static class CouponNotFoundException extends BusinessException {
        public CouponNotFoundException(Long couponId) {
            super("존재하지 않는 쿠폰입니다. 쿠폰 ID: " + couponId, "COUPON_NOT_FOUND");
        }
    }

    /**
     * 좌석 상태가 유효하지 않은 경우
     */
    public static class InvalidSeatStatusException extends BusinessException {
        public InvalidSeatStatusException(SeatStatus currentStatus) {
            super("좌석 상태가 유효하지 않습니다. 현재 상태: " + currentStatus, "INVALID_SEAT_STATUS");
        }
    }

    /**
     * 주문 상태가 유효하지 않은 경우
     */
    public static class InvalidOrderStatusException extends BusinessException {
        public InvalidOrderStatusException(OrderStatus currentStatus) {
            super("주문 상태가 유효하지 않습니다. 현재 상태: " + currentStatus, "INVALID_ORDER_STATUS");
        }
    }

    /**
     * 사용자를 찾을 수 없는 경우
     */
    public static class UserNotFoundException extends BusinessException {
        public UserNotFoundException(Long userId) {
            super("존재하지 않는 회원입니다. 회원 ID: " + userId, "USER_NOT_FOUND");
        }
    }
}