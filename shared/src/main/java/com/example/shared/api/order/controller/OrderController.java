package com.example.shared.api.order.controller;

import com.example.shared.api.order.service.OrderService;
import com.example.shared.domain.order.dto.OrderCancelDto;
import com.example.shared.domain.order.dto.OrderCreateDto;
import com.example.shared.domain.order.dto.OrderPaymentDto;
import com.example.shared.domain.order.dto.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
