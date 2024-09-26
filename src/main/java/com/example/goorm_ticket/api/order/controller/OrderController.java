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
        return orderService.order(eventId, orderDto);
    }

    @PostMapping("/orders/payment")
    public OrderDto.Response paymentCompleted(@RequestBody OrderDto.Payment orderDto) {
        return orderService.payed(orderDto);
    }


    @PostMapping("/orders/cancel/{eventId}")
    public OrderDto.Response cancel(@PathVariable Long eventId, @RequestBody OrderDto.Cancel orderDto) {
        return orderService.cancel(eventId, orderDto);
    }
}
