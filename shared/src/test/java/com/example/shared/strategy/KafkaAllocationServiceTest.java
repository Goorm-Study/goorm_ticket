package com.example.shared.strategy;

import com.example.shared.api.coupon.service.kafka.KafkaAllocationService;
import com.example.shared.domain.coupon.entity.Coupon;
import com.example.shared.domain.coupon.repository.CouponCountRepository;
import com.example.shared.domain.coupon.repository.CouponRepository;
import com.example.shared.domain.coupon.repository.UserCouponRepository;
import com.example.shared.domain.user.entity.User;
import com.example.shared.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class KafkaAllocationServiceTest {

    @Autowired
    private KafkaAllocationService kafkaAllocationService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponCountRepository couponCountRepository;

    @Autowired
    private UserCouponRepository userCouponRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    @DisplayName("1000명의 사용자가 쿠폰(100개)을 요청 시 100개만 발급된다.")
    @Test
    void allocateCouponTest() throws InterruptedException {
        // given
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        createUsers(1000);
        Coupon coupon = createCoupon(100L);
        String couponKey = "coupon" + coupon.getId();

        redisTemplate.opsForValue().set(couponKey, "0");
        
        // when
        for (int i = 1; i < threadCount+1; i++) {
            long userId = i;

            executorService.submit(() -> {
                try {
                    System.out.println("allocateCoupon 시작, userId: " + userId);
                    kafkaAllocationService.allocateCoupon(userId, 1L);
                    System.out.println("allocateCoupon 끝, userId: " + userId);

                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        Thread.sleep(10000);

        // then
        long count = userCouponRepository.count();
        Assertions.assertThat(count).isEqualTo(100); // 정확히 100개 발급
    }


    @DisplayName("요청이 실패하면 재시도하고, 재시도도 실패하면 DLQ처리된다.")
    @Test
    void allocateCouponTest_DLQ() throws InterruptedException {
        // given
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        createUsers(1000);
        Coupon coupon = createCoupon(100L);
        String couponKey = "coupon" + coupon.getId();

        redisTemplate.opsForValue().set(couponKey, "0");

        // when
        for (int i = 1; i < threadCount+1; i++) {
            long userId = i;

            executorService.submit(() -> {
                try {
                    kafkaAllocationService.allocateCoupon(userId, 1L);

                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        Thread.sleep(10000);

        // then
        long count = userCouponRepository.count();
        Long issuedCount = couponCountRepository.getIssuedCount(couponKey);

        Assertions.assertThat(count).isEqualTo(100); // 정확히 100개 발급
        Assertions.assertThat(issuedCount).isEqualTo(100); // Redis 서버에서도 정확히 100개 발급

    }

    @DisplayName("쿠폰카운트 확인")
    @Test
    void couponCount() {
        Long coupon1 = couponCountRepository.getIssuedCount("coupon1");

        System.out.println(coupon1);
    }

    @Test
    void createCoupon_Test() {
        Long quantity = 100L;
        Coupon coupon = Coupon.of(quantity, "coupon1", 0.15, LocalDateTime.of(2024, 12, 30, 0, 0));
        couponRepository.save(coupon);
        //couponCountRepository.setCouponQuantity(coupon.getId(), quantity); //쿠폰 초기수량
        System.out.println("couponId: " + coupon.getId());

    }

    private Coupon createCoupon(Long quantity) {
        Coupon coupon = Coupon.of(quantity, "coupon1", 0.15, LocalDateTime.of(2024, 12, 30, 0, 0));
        couponRepository.save(coupon);

        couponCountRepository.setCouponQuantity(coupon.getId(), quantity); //쿠폰 초기수량

        return couponRepository.save(coupon);
    }

    private void createUsers(int num) {
        for (int i=1; i<num+1; i++) {
            String userName = "user" + i;
            User user = User.builder().username(userName).password("1234").build();
            userRepository.save(user);
        }
    }

    private User createUser() {
        User user = User.builder().username("tester").password("1234").build();
        return userRepository.save(user);
    }

}