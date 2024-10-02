package com.example.goorm_ticket.api.order.controller;

import com.example.goorm_ticket.api.order.service.OrderService;
import com.example.goorm_ticket.domain.order.dto.OrderCancelDto;
import com.example.goorm_ticket.domain.order.dto.OrderCreateDto;
import com.example.goorm_ticket.domain.order.dto.OrderPaymentDto;
import com.example.goorm_ticket.domain.order.dto.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/{eventId}")
    public OrderResponseDto order(@PathVariable Long eventId, @RequestBody OrderCreateDto orderDto) {
        return orderService.order(eventId, orderDto);
    }

    @PostMapping("/payment")
    public OrderResponseDto paymentCompleted(@RequestBody OrderPaymentDto orderDto) {
        return orderService.payed(orderDto);
    }

    @PostMapping("/cancel/{eventId}")
    public OrderResponseDto cancel(@PathVariable Long eventId, @RequestBody OrderCancelDto orderDto) {
        return orderService.cancel(eventId, orderDto);
    }
}
