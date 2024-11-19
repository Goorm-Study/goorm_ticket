package com.example.goorm_ticket.api.coupon.service.strategy;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.example.goorm_ticket.api.coupon.service.CouponAllocationService;
import com.example.goorm_ticket.api.coupon.service.strategy.OptimisticLockStrategyTest.OptimisticLockStrategyConfig;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {OptimisticLockStrategyTest.OptimisticLockStrategyConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OptimisticLockStrategyTest {

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
    static class OptimisticLockStrategyConfig {
        @Bean
        @Primary
        public CouponAllocationStrategy couponAllocationStrategy(CouponRepository couponRepository, UserRepository userRepository) {
            return new OptimisticLockStrategy(couponRepository, userRepository);
        }
    }

    @Test
    @DisplayName("100개의 스레드에서 동시에 쿠폰(100개)을 요청한다 - 낙관적 락 적용, 재시도O")
    void allocateCouponTo100ThreadWithOptimisticLockRetry() throws InterruptedException {
        // given
        Coupon coupon = createCoupon(100L);
        User user = createUser();

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
                    CouponResponseDto couponResponseDto = couponAllocationService.allocateCouponToUser(user.getId(), coupon.getId());
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
        assertThat(successCount.get()).isEqualTo(100); // 성공한 쿠폰 발급이 100개인지 확인
        System.out.println("발급된 쿠폰 수량: " + successCount.get());

        // 재시도를 하지 않기 때문에 0개가 되지 않고 쿠폰이 남을 수 있음
        assertThat(foundCoupon.getQuantity()).isGreaterThanOrEqualTo(0);
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