package com.example.goorm_ticket.api.coupon.service.strategy;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import com.example.goorm_ticket.api.coupon.service.CouponAllocationService;
import com.example.goorm_ticket.api.coupon.service.strategy.NoLockStrategyTest.NoLockStrategyConfig;
import com.example.goorm_ticket.config.CouponStrategyConfig;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class NoLockStrategyTest {

    @MockBean(name = "optimisticLockStrategy")
    private CouponAllocationStrategy optimisticLockStrategy;

    @MockBean(name = "pessimisticLockStrategy")
    private CouponAllocationStrategy pessimisticLockStrategy;

    @MockBean(name = "redisLockStrategy")
    private CouponAllocationStrategy redisLockStrategy;

    @Autowired
    private CouponAllocationService couponAllocationService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserRepository userRepository;

    @TestConfiguration
     static class NoLockStrategyConfig {
        @Primary
        public CouponAllocationStrategy couponAllocationStrategy(CouponRepository couponRepository, UserRepository userRepository) {
            return new NoLockStrategy(couponRepository, userRepository);
        }
    }

    @Test
    @DisplayName("100개의 스레드에서 동시에 쿠폰(100개)을 요청한다 - 실패")
    void allocateCouponTo100Thread() throws InterruptedException {
        // given
        Coupon coupon = createCoupon(100L);
        User user = createUser();

        // when
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    couponAllocationService.allocateCouponToUser(user.getId(), coupon.getId());
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

    private Coupon createCoupon(Long quantity) {
        Coupon coupon = Coupon.of(quantity, "coupon1", 0.15, LocalDateTime.of(2024, 12, 30, 0, 0));
        return couponRepository.save(coupon);
    }

    private User createUser() {
        User user = User.builder().username("tester").password("1234").build();
        return userRepository.save(user);
    }

}