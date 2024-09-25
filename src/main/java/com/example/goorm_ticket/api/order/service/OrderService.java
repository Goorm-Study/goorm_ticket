package com.example.goorm_ticket.api.order.service;

import com.example.goorm_ticket.domain.order.repository.CouponRepository;
import com.example.goorm_ticket.domain.order.repository.OrderRepository;
import com.example.goorm_ticket.domain.order.repository.SeatRepository;
import com.example.goorm_ticket.domain.order.repository.UserRepository;
import com.example.goorm_ticket.domain.event.entity.Seat;
import com.example.goorm_ticket.domain.event.entity.SeatStatus;
import com.example.goorm_ticket.domain.order.dto.OrderDto;
import com.example.goorm_ticket.domain.order.entity.Order;
import com.example.goorm_ticket.domain.order.entity.OrderStatus;
import com.example.goorm_ticket.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;

    @Transactional
    public OrderDto.Response order(Long eventId, OrderDto.Create orderDto) {
        Long couponId = orderDto.getCouponId();
        Long userId = orderDto.getUserId();
        Long seatId = orderDto.getSeatId();

        Seat seat = seatRepository.findById(seatId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 쿠폰 할인 적용
        int ticketPrice = seat.getEvent().getTicketPrice();
        int discountPrice = 0;
        if (couponId != null) {
            Double discountRate = couponRepository.findById(couponId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."))
                    .getDiscountRate();
            discountPrice = (int) Math.floor(ticketPrice * discountRate / 100);
        }
        int totalPrice = ticketPrice - discountPrice;

        //orderStatus: `PENDING` (결제 완료 전)
        Order order = Order.createOrder(totalPrice, OrderStatus.PENDING, couponId, eventId, user);
        orderRepository.save(order);

        if (seat.getSeatStatus() != SeatStatus.AVAILABLE && seat.getSeatStatus() != SeatStatus.CANCELLED) {
            throw new IllegalArgumentException("이미 선택중인 좌석");
        }
        seat.update(order, SeatStatus.LOCKED);
        seatRepository.save(seat);

        return OrderDto.Response.builder().seatId(seatId).build();
    }


    // 티켓 결제 완료
    @Transactional
    public OrderDto.Response payed(Long orderId, OrderDto.Payment orderDto) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문"));

        Long seatId = orderDto.getSeatId();

        // orderStatus: PENDING → CONFIRMED
        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("결제할 수 없는 주문입니다.");
        }
        order.update(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        Seat seat = seatRepository.findById(seatId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));

        // seatStatus: `Locked` → `RESERVED`
        if (seat.getSeatStatus() != SeatStatus.LOCKED) {
            throw new IllegalArgumentException("결제할 수 없는 좌석입니다");
        }
        seat.update(order, SeatStatus.RESERVED);
        seatRepository.save(seat);

        return OrderDto.Response.builder().seatId(seatId).build();
    }


    // 주문 취소
    @Transactional
    public OrderDto.Response cancel(Long eventId, OrderDto.Cancel orderDto) {
        Long seatId = orderDto.getSeatId();
        Long userId = orderDto.getUserId();

        Seat seat = seatRepository.findById(seatId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석"));

        Order order = seat.getOrder();

        //orderStatus: `CANCELLED`
        order.update(OrderStatus.CANCELLED);
        orderRepository.save(order);


        if (seat.getSeatStatus() != SeatStatus.RESERVED && seat.getSeatStatus() != SeatStatus.LOCKED) {
            throw new IllegalArgumentException("취소할 수 없는 좌석");
        }
        seat.update(order, SeatStatus.CANCELLED);
        seatRepository.save(seat);

        return OrderDto.Response.builder().seatId(seatId).build();
    }
}
