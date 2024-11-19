//package com.example.goorm_ticket.api.coupon.service;
//
//import com.example.goorm_ticket.api.coupon.service.strategy.CouponAllocationStrategy;
//import com.example.goorm_ticket.api.coupon.service.strategy.NoLockStrategy;
//import com.example.goorm_ticket.api.coupon.service.strategy.OptimisticLockStrategy;
//import com.example.goorm_ticket.api.coupon.service.strategy.PessimisticLockStrategy;
//import com.example.goorm_ticket.api.coupon.service.strategy.RedisLockStrategy;
//import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
//import com.example.goorm_ticket.domain.coupon.entity.Coupon;
//import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
//import com.example.goorm_ticket.domain.user.entity.User;
//import com.example.goorm_ticket.domain.user.repository.UserRepository;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Import;
//import org.springframework.context.annotation.Primary;
//import org.springframework.orm.ObjectOptimisticLockingFailureException;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicInteger;
//import org.springframework.test.annotation.DirtiesContext;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//
//@SpringBootTest
//class CouponAllocationServiceConcurencyTest {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private CouponRepository couponRepository;
//
//    @Autowired
//    private CouponAllocationService couponAllocationService;
//
//
//
//    @AfterEach
//    void tearDown() {
//        userRepository.deleteAll();
//        couponRepository.deleteAll();
//    }
//
//    // 전략마다 별도 @TestConfiguration을 작성하여 필요할 때 설정을 변경
//    @TestConfiguration
//    static class NoLockStrategyConfig {
//        @Bean
//        public CouponAllocationStrategy couponAllocationStrategy(CouponRepository couponRepository, UserRepository userRepository) {
//            return new NoLockStrategy(couponRepository, userRepository);
//        }
//    }
//
//    @TestConfiguration
//    static class PessimisticLockStrategyConfig {
//        @Bean
//        public CouponAllocationStrategy couponAllocationStrategy(CouponRepository couponRepository, UserRepository userRepository) {
//            return new PessimisticLockStrategy(couponRepository, userRepository);
//        }
//    }
//
//
//
//
//
//
//
//
//
//
//
////    @Test
////    @DisplayName("100개의 스레드에서 동시에 쿠폰(10개)을 요청한다 - 낙관적 락 적용, 재시도X도 성공")
////    @Import(OptimisticLockStrategyConfig.class)
////    @DirtiesContext
////    void allocateCouponTo100ThreadWithOptimisticLock() throws InterruptedException {
////        // given
////        Coupon coupon = Coupon.of(10L,
////                "coupon1",
////                0.15,
////                LocalDateTime.of(2024, 12, 30, 0, 0)
////        );
////
////        couponRepository.save(coupon);
////
////        User user = User.builder()
////                .username("tester")
////                .password("1234")
////                .build();
////
////        userRepository.save(user);
////
////        // when
////        int threadCount = 100;
////        ExecutorService executorService = Executors.newFixedThreadPool(32);
////        CountDownLatch latch = new CountDownLatch(threadCount);
////        List<Future<CouponResponseDto>> futures = new ArrayList<>();
////        AtomicInteger successCount = new AtomicInteger(0);
////        AtomicInteger failureCount = new AtomicInteger(0);
////
////        for (int i = 0; i < threadCount; i++) {
////            futures.add(executorService.submit(() -> {
////                try {
////                    // 낙관적 락이 걸린 쿠폰 발급 서비스 호출
////                    CouponResponseDto couponResponseDto = couponAllocationService.allocateCouponToUser(user.getId(), coupon.getId());
////                    System.out.println("쿠폰 발급 완료");
////                    successCount.incrementAndGet();
////                    return couponResponseDto;
////                } catch (ObjectOptimisticLockingFailureException e) {
////                    System.out.println("낙관적 락 충돌 발생: " + e.getMessage());
////                    failureCount.incrementAndGet();
////                    return null;
////                } finally {
////                    latch.countDown();
////                }
////            }));
////        }
////
////        latch.await();
////
////        Coupon foundCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
////        System.out.println("남은 쿠폰 수량: " + foundCoupon.getQuantity());
////
////        // then
////        assertThat(successCount.get()).isEqualTo(10); // 성공한 쿠폰 발급이 10개인지 확인
////        System.out.println("발급된 쿠폰 수량: " + successCount.get());
////
////        // 재시도를 하지 않기 때문에 0개가 되지 않고 쿠폰이 남을 수 있음
////        assertThat(foundCoupon.getQuantity()).isGreaterThanOrEqualTo(0);
////    }
//
//
//
//
//
//    // 유저 생성 메서드
//    private User createUser() {
//        User user = User.builder()
//                .username("tester")
//                .password("1234")
//                .build();
//        return userRepository.save(user);
//    }
//
//}