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
        Double discountRate = couponRepository.findById(couponId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."))
                .getDiscountRate();


        int ticketPrice = seat.getEvent().getTicketPrice();
        int discountPrice = (int) Math.floor(ticketPrice * discountRate / 100);
        int totalPrice = ticketPrice - discountPrice;


        //orderStatus: `PENDING` (결제 완료 전)
        Order order = Order.createOrder(totalPrice, OrderStatus.PENDING, couponId, eventId, user);
        orderRepository.save(order);

        try {
            // seatStatus: `AVAILABLE` → `Locked`  (아직 예약 완료는 아님)
            if (seat.getSeatStatus() == SeatStatus.AVAILABLE) {
                seat.update(order, SeatStatus.LOCKED);
                seatRepository.save(seat);
            } else {
                throw new Exception("이미 선택중인 좌석");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return OrderDto.Response.builder().seatId(seatId).build();
    }


    // 티켓 결제



    // 주문 취소
    public OrderDto.Response cancel(Long eventId, OrderDto.Cancel orderDto) {
        Long seatId = orderDto.getSeatId();
        Long userId = orderDto.getUserId();


        return OrderDto.Response.builder().seatId(seatId).build();
    }
}
