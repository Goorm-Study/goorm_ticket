package com.example.goorm_ticket.api.coupon.service.strategy;

import com.example.goorm_ticket.api.coupon.service.kafka.KafkaAllocationService;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.repository.CouponCountRepository;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.coupon.repository.UserCouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
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
        Assertions.assertThat(count).isEqualTo(100); // 정확히 100개 발급
    }


    private Coupon createCoupon(Long quantity) {
        Coupon coupon = Coupon.of(quantity, "coupon1", 0.15, LocalDateTime.of(2024, 12, 30, 0, 0));
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