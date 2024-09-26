package com.example.goorm_ticket.api.order.service;

import com.example.goorm_ticket.api.order.exception.BusinessException;
import com.example.goorm_ticket.api.order.utils.DiscountCalculator;
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

import java.util.Arrays;

import static com.example.goorm_ticket.api.order.exception.BusinessException.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;

    /**
     * 주문 생성
     */
    @Transactional
    public OrderDto.Response order(Long eventId, OrderDto.Create orderDto) {
        Long couponId = orderDto.getCouponId();
        Long userId = orderDto.getUserId();
        Long seatId = orderDto.getSeatId();

        Seat seat = findSeatWithEvent(seatId);
        User user = findUserById(userId);

        // 이벤트 ID 일치 여부 확인
        if (!seat.getEvent().getId().equals(eventId)) {
            throw new BusinessException.EventMismatchException(eventId, seat.getEvent().getId());
        }

        // 쿠폰 할인 적용
        int ticketPrice = seat.getEvent().getTicketPrice();
        int discountPrice = applyCouponDiscount(couponId, ticketPrice);
        int totalPrice = ticketPrice - discountPrice;

        // 주문생성 orderStatus: `PENDING`
        Order order = Order.createOrder(totalPrice, OrderStatus.PENDING, couponId, eventId, user);
        orderRepository.save(order);

        // 좌석 상태 변경 LOCKED
        validateSeatStatus(seat, SeatStatus.AVAILABLE, SeatStatus.CANCELLED);
        seat.update(order, SeatStatus.LOCKED);

        return OrderDto.Response.builder().seatId(seatId).build();
    }

    /**
     * 결제 완료
     */
    @Transactional
    public OrderDto.Response payed(OrderDto.Payment orderDto) {
        Long seatId = orderDto.getSeatId();

        Seat seat = findSeatById(seatId);
        Order order = findOrderBySeat(seat);

        // 주문 상태 변경: PENDING → CONFIRMED
        validateOrderStatus(order, OrderStatus.PENDING);
        order.update(OrderStatus.CONFIRMED);

        // 좌석 상태 변경: LOCKED -> RESERVED
        validateSeatStatus(seat, SeatStatus.LOCKED);
        seat.update(order, SeatStatus.RESERVED);

        return OrderDto.Response.builder().seatId(seatId).build();
    }

    /**
     * 주문 취소
     */
    @Transactional
    public OrderDto.Response cancel(Long eventId, OrderDto.Cancel orderDto) {
        Long seatId = orderDto.getSeatId();

        Seat seat = findSeatById(seatId);
        Order order = findOrderBySeat(seat);

        // 이벤트 ID 일치 여부 확인
        if (!seat.getEvent().getId().equals(eventId)) {
            throw new BusinessException.EventMismatchException(eventId, seat.getEvent().getId());
        }

        // 주문 상태 변경: CANCELLED
        validateOrderStatus(order, OrderStatus.CONFIRMED, OrderStatus.PENDING);
        order.update(OrderStatus.CANCELLED);

        // 좌석 상태 변경: RESERVED, LOCKED → CANCELLED
        validateSeatStatus(seat, SeatStatus.RESERVED, SeatStatus.LOCKED);
        seat.update(order, SeatStatus.CANCELLED);

        return OrderDto.Response.builder().seatId(seatId).build();
    }

    /**
     * 좌석 조회
     */
    public Seat findSeatById(Long seatId) {
        return seatRepository.findById(seatId)
                .orElseThrow(() -> new SeatNotFoundException(seatId));
    }

    /**
     * 좌석 및 이벤트 조회
     */
    public Seat findSeatWithEvent(Long seatId) {
        return seatRepository.findByIdWithEvent(seatId)
                .orElseThrow(() -> new SeatNotFoundException(seatId));
    }

    /**
     * 사용자 조회
     */
    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    /**
     * 좌석에 연결된 주문 조회
     */
    public Order findOrderBySeat(Seat seat) {
        Order order = seat.getOrder();
        if (order == null) {
            throw new OrderNotFoundException(seat.getId());
        }
        return order;
    }

    /**
     * 쿠폰 할인 적용 로직
     */
    public int applyCouponDiscount(Long couponId, int ticketPrice) {
        if (couponId != null) {
            Double discountRate = couponRepository.findById(couponId)
                    .orElseThrow(() -> new CouponNotFoundException(couponId))
                    .getDiscountRate();

            return DiscountCalculator.calculateDiscount(ticketPrice, discountRate);
        }
        return 0;
    }

    // 좌석 상태 유효성 검증 메서드: 상태가 유효하지 않으면 예외를 던진다
    private void validateSeatStatus(Seat seat, SeatStatus... validStatuses) {
        if (Arrays.stream(validStatuses).noneMatch(status -> status == seat.getSeatStatus())) {
            throw new InvalidSeatStatusException(seat.getSeatStatus());
        }
    }

    // 주문 상태 유효성 검증 메서드: 상태가 유효하지 않으면 예외를 던진다
    private void validateOrderStatus(Order order, OrderStatus... validStatuses) {
        if (Arrays.stream(validStatuses).noneMatch(status -> status == order.getOrderStatus())) {
            throw new InvalidOrderStatusException(order.getOrderStatus());
        }
    }
}
