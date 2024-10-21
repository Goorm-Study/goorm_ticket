package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PessimisticLockCouponServiceTest {
    @Autowired
    private PessimisticLockCouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    public void after() {
        couponRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test // 아직 동시성 처리 안해서 테스트 실패함
    @DisplayName("100개의 스레드에서 동시에 쿠폰을 요청한다.")
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
                    couponService.allocateCouponToUserWithPessimisticLock(user.getId(), coupon.getId());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Coupon foundCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
        assertEquals(0, foundCoupon.getQuantity());
    }

}