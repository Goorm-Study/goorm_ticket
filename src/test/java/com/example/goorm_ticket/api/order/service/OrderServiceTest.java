package com.example.goorm_ticket.api.order.service;

import com.example.goorm_ticket.api.order.exception.BusinessException;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.event.repository.SeatRepository;
import com.example.goorm_ticket.domain.event.entity.Event;
import com.example.goorm_ticket.domain.event.entity.Seat;
import com.example.goorm_ticket.domain.event.entity.SeatStatus;
import com.example.goorm_ticket.domain.event.repository.EventRepository;
import com.example.goorm_ticket.domain.order.dto.OrderCreateDto;
import com.example.goorm_ticket.domain.order.entity.Order;
import com.example.goorm_ticket.domain.order.repository.OrderRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    EventRepository eventRepository;
    @Autowired
    SeatRepository seatRepository;
    @Autowired
    CouponRepository couponRepository;
    @Autowired
    OrderService orderService;
    @Test
    public void seatOrder() throws InterruptedException, ExecutionException {
        //given
        User user = User.builder()
                .username("tester")
                .password("1234")
                .build();
        userRepository.save(user);

        Event event = Event.of("artist", "event1", "event", "14", "place1", 36000, LocalDateTime.now());
        eventRepository.save(event);

        Seat seat = Seat.builder()
                .seatNumber(1L)
                .seatSection("R")
                .seatStatus(SeatStatus.AVAILABLE)
                .event(event)
                .build();
        seatRepository.save(seat);

        Coupon coupon = Coupon.of(100L,
                "coupon1",
                0.15,
                LocalDateTime.of(2024, 12, 30, 0, 0)
        );
        couponRepository.save(coupon);

        // when
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        List<Future<Boolean>> results = new ArrayList<>();

        for(int i = 0; i < threadCount; i++) {
            Future<Boolean> result = executorService.submit(() -> {
                try {
                    orderService.order(event.getId(), OrderCreateDto.of(user.getId(), seat.getId(), coupon.getId()));
                    return true;
                } catch (BusinessException.InvalidSeatStatusException e){
                    return false;
                }
                finally {
                    latch.countDown();
                }
            });
            results.add(result);
        }

        latch.await();

        // then
        int orderSuccessCount = (int) results.stream().filter(booleanFuture -> {
            try {
                return booleanFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }).count(); // 성공한 예약
        int orderFailCount = 100 - orderSuccessCount; // 실패한 예약

        assertThat(orderSuccessCount).isEqualTo(1);
        assertThat(orderFailCount).isEqualTo(99);
        // assertThat(results.get(0).get()).isEqualTo(true);
    }

}