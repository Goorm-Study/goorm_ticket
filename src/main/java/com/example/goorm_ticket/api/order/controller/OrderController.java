package com.example.goorm_ticket.api.order.controller;

import com.example.goorm_ticket.api.order.service.OrderService;
import com.example.goorm_ticket.domain.order.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders/{eventId}")
    public OrderDto.Response order(@PathVariable Long eventId, @RequestBody OrderDto.Create orderDto) {
        OrderDto.Response response = orderService.order(eventId, orderDto);
        return response;
    }

    @PostMapping("/orders/payment/{orderId}")
    public OrderDto.Response paymentCompleted(@PathVariable Long orderId, @RequestBody OrderDto.Payment orderDto) {
        return orderService.payed(orderId, orderDto);
    }


    @PostMapping("/orders/cancel/{eventId}")
    public OrderDto.Response cancel(@PathVariable Long eventId, @RequestBody OrderDto.Cancel orderDto) {
        return orderService.cancel(eventId, orderDto);
    }
}
