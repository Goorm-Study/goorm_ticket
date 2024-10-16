package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.api.coupon.exception.CouponException;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CouponServiceConcurencyTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponService couponService;


    @Test // 아직 동시성 처리 안해서 테스트 실패함
    @DisplayName("100개의 스레드에서 동시에 쿠폰(100개)을 요청한다 - 실패")
    void allocateCouponTo100Thread() throws InterruptedException {
        // given
        Coupon coupon = Coupon.of(100L,
                "coupon1",
                0.15,
                LocalDateTime.of(2024, 12, 30, 0, 0)
        );

        couponRepository.save(coupon);

        User user = User.builder()
                .username("tester")
                .password("1234")
                .build();

        userRepository.save(user);

        // when
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    couponService.allocateCouponToUser(user.getId(), coupon.getId());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Coupon foundCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
        System.out.println("남은 쿠폰 수량: " + foundCoupon.getQuantity());
        assertThat(foundCoupon.getQuantity()).isNotEqualTo(0); //동시성 제어에 실패할 것
    }

    @Test
    @DisplayName("100개의 스레드에서 동시에 쿠폰(100개)을 요청한다 - 비관적 락")
    void allocateCouponTo100ThreadWithPessisticLock() throws InterruptedException {
        // given
        Coupon coupon = Coupon.of(100L,
                "coupon1",
                0.15,
                LocalDateTime.of(2024, 12, 30, 0, 0)
        );

        couponRepository.save(coupon);

        User user = User.builder()
                .username("tester")
                .password("1234")
                .build();

        userRepository.save(user);

        // when
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    couponService.allocateCouponToUserWithPessimisticLock(user.getId(), coupon.getId());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Coupon foundCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
        System.out.println("남은 쿠폰 수량: " + foundCoupon.getQuantity());
        assertThat(foundCoupon.getQuantity()).isEqualTo(0); //동시성 제어 성공
    }

    @Test
    @DisplayName("100개의 스레드에서 동시에 쿠폰(10개)을 요청한다 - 낙관적 락 적용, 재시도X")
    void allocateCouponTo100ThreadWithOptimisticLock() throws InterruptedException {
        // given
        Coupon coupon = Coupon.of(10L,
                "coupon1",
                0.15,
                LocalDateTime.of(2024, 12, 30, 0, 0)
        );

        couponRepository.save(coupon);

        User user = User.builder()
                .username("tester")
                .password("1234")
                .build();

        userRepository.save(user);

        // when
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Future<CouponResponseDto>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            futures.add(executorService.submit(() -> {
                try {
                    // 낙관적 락이 걸린 쿠폰 발급 서비스 호출
                    CouponResponseDto couponResponseDto = couponService.allocateCouponToUserWithOptimisticLock(user.getId(), coupon.getId());
                    System.out.println("쿠폰 발급 완료");
                    successCount.incrementAndGet();
                    return couponResponseDto;
                } catch (ObjectOptimisticLockingFailureException e) {
                    System.out.println("낙관적 락 충돌 발생: " + e.getMessage());
                    failureCount.incrementAndGet();
                    return null;
                } finally {
                    latch.countDown();
                }
            }));
        }

        latch.await();

        Coupon foundCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
        System.out.println("남은 쿠폰 수량: " + foundCoupon.getQuantity());

        // then
        assertThat(successCount.get()).isEqualTo(10); // 성공한 쿠폰 발급이 10개인지 확인
        System.out.println("발급된 쿠폰 수량: " + successCount.get());

        // 재시도를 하지 않기 때문에 0개가 되지 않고 쿠폰이 남을 수 있음
        assertThat(foundCoupon.getQuantity()).isGreaterThanOrEqualTo(0);
    }
}