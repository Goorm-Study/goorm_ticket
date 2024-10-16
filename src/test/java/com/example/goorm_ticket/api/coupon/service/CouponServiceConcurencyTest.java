package com.example.goorm_ticket.api.coupon.service;

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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

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
    @DisplayName("100개의 스레드에서 동시에 쿠폰을 요청한다 - 실패")
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

    @Test // 아직 동시성 처리 안해서 테스트 실패함
    @DisplayName("100개의 스레드에서 동시에 쿠폰을 요청한다 - 비관적")
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
}