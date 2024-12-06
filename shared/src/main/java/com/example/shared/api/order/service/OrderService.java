package com.example.shared.api.order.service;

import com.example.shared.api.coupon.exception.CouponException.CouponNotFoundException;
import com.example.shared.api.coupon.exception.CouponException.UserNotFoundException;
import com.example.shared.api.order.exception.BusinessException.EventMismatchException;
import com.example.shared.api.order.exception.BusinessException.InvalidOrderStatusException;
import com.example.shared.api.order.exception.BusinessException.InvalidSeatStatusException;
import com.example.shared.api.order.exception.BusinessException.OrderNotFoundException;
import com.example.shared.api.order.exception.BusinessException.SeatNotFoundException;
import com.example.shared.api.order.utils.DiscountCalculator;
import com.example.shared.domain.coupon.repository.CouponRepository;
import com.example.shared.domain.event.entity.Seat;
import com.example.shared.domain.event.entity.SeatStatus;
import com.example.shared.domain.event.repository.SeatRepository;
import com.example.shared.domain.order.dto.OrderCancelDto;
import com.example.shared.domain.order.dto.OrderCreateDto;
import com.example.shared.domain.order.dto.OrderPaymentDto;
import com.example.shared.domain.order.dto.OrderResponseDto;
import com.example.shared.domain.order.entity.Order;
import com.example.shared.domain.order.entity.OrderStatus;
import com.example.shared.domain.order.repository.OrderRepository;
import com.example.shared.domain.user.entity.User;
import com.example.shared.domain.user.repository.UserRepository;
import java.util.Arrays;
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

    /**
     * 주문 생성
     */
    @Transactional
    public OrderResponseDto order(Long eventId, OrderCreateDto orderDto) {
        Long couponId = orderDto.getCouponId();
        Long userId = orderDto.getUserId();
        Long seatId = orderDto.getSeatId();
        System.out.println("1차 로그");

        Seat seat = findSeatWithEvent(seatId);
        System.out.println("2차 로그");
        User user = findUserById(userId);
        System.out.println("3차 로그");

        // 이벤트 ID 일치 여부 확인
        if (!seat.getEvent().getId().equals(eventId)) {
            throw new EventMismatchException(eventId, seat.getEvent().getId());
        }

        // 쿠폰 할인 적용
        int ticketPrice = seat.getEvent().getTicketPrice();
        int discountPrice = applyCouponDiscount(couponId, ticketPrice);
        int totalPrice = ticketPrice - discountPrice;

        // 주문생성 orderStatus: `PENDING`
        Order order = Order.of(totalPrice, OrderStatus.PENDING, couponId, eventId, user);
        System.out.println("4차 로그");
        orderRepository.save(order);

        // 좌석 상태 변경 LOCKED
        validateSeatStatus(seat, SeatStatus.AVAILABLE, SeatStatus.CANCELLED);
        seat.update(order, SeatStatus.LOCKED);

        return OrderResponseDto.builder().seatId(seatId).build();
    }

    /**
     * 결제 완료
     */
    @Transactional
    public OrderResponseDto payed(OrderPaymentDto orderDto) {
        Long seatId = orderDto.getSeatId();

        Seat seat = findSeatById(seatId);
        Order order = findOrderBySeat(seat);

        // 주문 상태 변경: PENDING → CONFIRMED
        validateOrderStatus(order, OrderStatus.PENDING);
        order.update(OrderStatus.CONFIRMED);

        // 좌석 상태 변경: LOCKED -> RESERVED
        validateSeatStatus(seat, SeatStatus.LOCKED);
        seat.update(order, SeatStatus.RESERVED);

        return OrderResponseDto.builder().seatId(seatId).build();
    }

    /**
     * 주문 취소
     */
    @Transactional
    public OrderResponseDto cancel(Long eventId, OrderCancelDto orderDto) {
        Long seatId = orderDto.getSeatId();

        Seat seat = findSeatByIdWithLock(seatId);
        Order order = findOrderBySeat(seat);

        // 이벤트 ID 일치 여부 확인
        if (!seat.getEvent().getId().equals(eventId)) {
            throw new EventMismatchException(eventId, seat.getEvent().getId());
        }

        // 주문 상태 변경: CANCELLED
        validateOrderStatus(order, OrderStatus.CONFIRMED, OrderStatus.PENDING);
        order.update(OrderStatus.CANCELLED);

        // 좌석 상태 변경: RESERVED, LOCKED → CANCELLED
        validateSeatStatus(seat, SeatStatus.RESERVED, SeatStatus.LOCKED);
        seat.update(order, SeatStatus.CANCELLED);

        return OrderResponseDto.builder().seatId(seatId).build();
    }

    /**
     * 좌석 조회
     */
    public Seat findSeatById(Long seatId) {
        return seatRepository.findById(seatId)
                .orElseThrow(() -> new SeatNotFoundException(seatId));
    }

    @Transactional
    public Seat findSeatByIdWithLock(Long seatId) {
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
