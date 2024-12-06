package com.example.shared.distribute;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.example.shared.api.coupon.service.distribute.LockTestService;
import com.example.shared.domain.coupon.entity.Coupon;
import com.example.shared.domain.coupon.repository.CouponRepository;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LockTestServiceTest {

    @Autowired
    private LockTestService lockTestService;

    @Autowired
    private CouponRepository couponRepository;

    private Coupon coupon;

    @BeforeEach
    public void setup() {
        // 테스트용 쿠폰 저장
        coupon = couponRepository.save(Coupon.of(100L, "Test Coupon", 0.2, LocalDateTime.now().plusDays(5)));
    }

    @Test
    public void testDecreaseCouponQuantity() {
        String result = lockTestService.decreaseCouponQuantity(coupon.getId(), 5L);
        System.out.println("메서드 결과: " + result);

        // 출력 로그를 통해 AOP와 비즈니스 로직이 제대로 실행되었는지 확인
        // 예상 로그:
        // AOP가 실행되었습니다.
        // SpEL Evaluation Result: LOCK:<couponId>
        // 쿠폰 감소 비즈니스 로직 실행: <couponId>
    }

    @Test
    void 쿠폰차감_분산락_적용_동시성100명_테스트() throws InterruptedException {
        int numberOfThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    // 각 스레드가 쿠폰을 1씩 차감하도록 메서드 호출
                    lockTestService.decreaseCouponQuantity(coupon.getId(), 1L);
                    Coupon persistCoupon = couponRepository.findById(coupon.getId())
                            .orElseThrow(IllegalArgumentException::new);

                    System.out.println("잔여 쿠폰 개수 = " + persistCoupon.getQuantity());
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 스레드가 작업을 완료할 때까지 대기
        latch.await();

        // 쿠폰의 최종 재고 수량을 조회

        Coupon persistCoupon = couponRepository.findById(coupon.getId())
                .orElseThrow(IllegalArgumentException::new);

        System.out.println("잔여 쿠폰 개수 = " + persistCoupon.getQuantity());
        assertThat(persistCoupon.getQuantity()).isZero();

    }

}