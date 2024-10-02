package com.example.goorm_ticket.domain.user.dto;

import com.example.goorm_ticket.domain.order.entity.Order;
import com.example.goorm_ticket.domain.order.entity.Order.OrderStatus;

import java.time.LocalDateTime;

public record UserOrderResponseDto(
    Long id,
    LocalDateTime reservationDate,
    Long paymentAmount,
    OrderStatus orderStatus,
    Long orderID
) {
    public static UserOrderResponseDto of(Order order) {
        return new UserOrderResponseDto(
                order.getId(),
                order.getReservationDate(),
                order.getPaymentAmount(),
                order.getOrderStatus(),
                order.getOrderID()
        );
    }
}
