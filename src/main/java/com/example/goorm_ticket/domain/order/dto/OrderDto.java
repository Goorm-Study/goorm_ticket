package com.example.goorm_ticket.domain.order.dto;

import com.example.goorm_ticket.domain.order.entity.Order;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderDto {

    @Getter
    @Builder
    public static class Create {
        private Long userId;
        private Long seatId;
        private Long couponId;
    }

    @Getter
    @Builder
    public static class Response {
        private Long seatId;
    }

    @Getter
    public static class Cancel {
        private Long userId;
        private Long seatId;
    }

}
