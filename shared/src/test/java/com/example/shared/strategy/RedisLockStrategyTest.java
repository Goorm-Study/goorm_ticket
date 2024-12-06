package com.example.shared.strategy;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.example.shared.api.coupon.service.distribute.RedisLockStrategy;
import com.example.shared.api.coupon.service.strategy.CouponAllocationStrategy;
import com.example.shared.domain.coupon.entity.Coupon;
import com.example.shared.domain.coupon.repository.CouponRepository;
import com.example.shared.domain.user.entity.User;
import com.example.shared.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {RedisLockStrategyTest.RedisLockStrategyConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RedisLockStrategyTest {

    @MockBean(name = "optimisticLockStrategy")
    private CouponAllocationStrategy optimisticLockStrategy;

    @MockBean(name = "pessimisticLockStrategy")
    private CouponAllocationStrategy pessimisticLockStrategy;

    @Autowired
    private RedisLockStrategy couponAllocationService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserRepository userRepository;

    @TestConfiguration
    static class RedisLockStrategyConfig {
        @Bean
        @Primary
        public RedisLockStrategy couponAllocationStrategy(CouponRepository couponRepository, UserRepository userRepository) {
            return new RedisLockStrategy(couponRepository, userRepository);
        }
    }

//    @Test
//    @DisplayName("100개의 스레드에서 동시에 쿠폰(100개)을 요청한다 - 분산락")
//    void allocateCouponTo100ThreadWithRedisLock() throws InterruptedException {
//        // given
//        Coupon coupon = createCoupon(100L);
//        User user = createUser();
//
//        AtomicInteger successCount = new AtomicInteger(0);
//
//        // when
//        int threadCount = 100;
//        ExecutorService executorService = Executors.newFixedThreadPool(32);
//        CountDownLatch latch = new CountDownLatch(threadCount);
//
//        for(int i = 0; i < threadCount; i++) {
//            executorService.submit(() -> {
//                try {
//                    couponAllocationService.allocateCoupon(user.getId(), coupon.getId());
//                    successCount.incrementAndGet();
//                    System.out.println("successCount = " + successCount);
//                    System.out.println("couponCount= " + coupon.getQuantity());
//
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        latch.await();
//
//        Coupon foundCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
//        System.out.println("남은 쿠폰 수량: " + foundCoupon.getQuantity());
//        System.out.println("발급된 쿠폰 수량: " + successCount);
//        assertThat(foundCoupon.getQuantity()).isEqualTo(0); //동시성 제어 성공
//    }

    @Test
    void 쿠폰차감_분산락_적용_동시성100명_테스트() throws InterruptedException {
        Coupon coupon = createCoupon(100L);
        couponRepository.save(coupon);
        RedisLockStrategy couponService = new RedisLockStrategy(couponRepository,userRepository);

        int numberOfThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    // 분산락 적용 메서드 호출 (락의 key는 쿠폰의 name으로 설정)
                    couponService.couponDecrease(coupon.getId());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Coupon persistCoupon = couponRepository.findById(coupon.getId())
                .orElseThrow(IllegalArgumentException::new);

        assertThat(persistCoupon.getQuantity()).isZero();
        System.out.println("잔여 쿠폰 개수 = " + persistCoupon.getQuantity());
    }

    @Test
    void 쿠폰차감_분산락_적용_동시성100명_테스트2() throws InterruptedException {
        Coupon coupon = createCoupon(100L);
        couponRepository.save(coupon);
        RedisLockStrategy couponService = new RedisLockStrategy(couponRepository,userRepository);

        int numberOfThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    // 분산락 적용 메서드 호출 (락의 key는 쿠폰의 name으로 설정)
                    couponService.couponDecrease(coupon.getId());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Coupon persistCoupon = couponRepository.findById(coupon.getId())
                .orElseThrow(IllegalArgumentException::new);

        assertThat(persistCoupon.getQuantity()).isZero();
        System.out.println("잔여 쿠폰 개수 = " + persistCoupon.getQuantity());
    }

    //쿠폰 생성 메서드
    private Coupon createCoupon(Long quantity) {
        Coupon coupon = Coupon.of(quantity,
                "coupon1",
                0.15,
                LocalDateTime.of(2024, 12, 30, 0, 0)
        );
        return couponRepository.save(coupon);
    }

    private User createUser() {
        User user = User.builder()
                .username("tester")
                .password("1234")
                .build();
        return userRepository.save(user);
    }

}