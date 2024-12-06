package com.example.shared.domain.user.dto;

import com.example.shared.domain.order.entity.Order;
import com.example.shared.domain.order.entity.OrderStatus;
import java.time.LocalDateTime;

public record UserOrderResponseDto(
        Long id,
        LocalDateTime reservationDate,
        int paymentAmount,
        OrderStatus orderStatus
) {
    public static UserOrderResponseDto of(Order order) {
        return new UserOrderResponseDto(
                order.getId(),
                order.getReservationDate(),
                order.getPaymentAmount(),
                order.getOrderStatus()
        );
    }
}