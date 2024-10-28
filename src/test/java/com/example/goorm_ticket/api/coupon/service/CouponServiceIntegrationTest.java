package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.example.goorm_ticket.api.coupon.exception.CouponException.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CouponServiceIntegrationTest {
    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    public void after() {
        couponRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("쿠폰 2개를 넣은 후 전체 쿠폰 목록을 조회한다.")
    void getAllCoupons() {
        // given
        Coupon coupon1 = createCoupon("coupon1");
        Coupon coupon2 = createCoupon("coupon2");
        couponRepository.saveAll(List.of(coupon1, coupon2));

        // when
        List<CouponResponseDto> couponList = couponService.getAllCoupons();

        // then
        assertThat(couponList).hasSize(2);
        assertThat(couponList.get(0).getId()).isEqualTo(coupon1.getId());
        assertThat(couponList.get(1).getId()).isEqualTo(coupon2.getId());
    }

    private static User createUser(String username) {
        return User.builder()
                .username(username)
                .password("1234")
                .build();
    }

    private static Coupon createCoupon(String couponName) {
        return Coupon.of(100L,
                couponName,
                0.15,
                LocalDateTime.of(2024, 12, 30, 0, 0)
        );
    }

    @Test
    @Transactional // 지연 로딩이므로 영속성 컨텍스트가 초기화를 시켜줘야 하므로 트랜잭션 처리
    @DisplayName("유저 쿠폰 목록에 쿠폰을 2개 넣은 후 유저 쿠폰 목록을 조회한다.")
    void getUserCoupons() {
        // given
        User user = createUser("tester");
        userRepository.save(user);

        Coupon coupon1 = createCoupon("coupon1");
        Coupon coupon2 = createCoupon("coupon2");
        couponRepository.saveAll(List.of(coupon1, coupon2));

        List<CouponEmbeddable> coupons = user.getCoupons();
        coupons.add(CouponEmbeddable.of(coupon1.getId(), coupon1.getName()));
        coupons.add(CouponEmbeddable.of(coupon2.getId(), coupon2.getName()));
        userRepository.save(user);

        // when
        List<CouponResponseDto> userCoupons = couponService.getUserCoupons(user.getId());

        // then
        assertThat(userCoupons).hasSize(2);
        assertThat(userCoupons.get(0).getId()).isEqualTo(coupon1.getId());
        assertThat(userCoupons.get(1).getId()).isEqualTo(coupon2.getId());
    }

    @Test
    @Transactional // 지연 로딩이므로 영속성 컨텍스트가 초기화를 시켜줘야 하므로 트랜잭션 처리
    @DisplayName("존재하지 않는 유저의 경우 유저의 쿠폰 목록을 확인할 수 없다.")
    void getUserCoupons_userNotFound() {
        // given
        User user = createUser("tester");

        userRepository.save(user);

        Coupon coupon1 = createCoupon("coupon1");
        Coupon coupon2 = createCoupon("coupon2");
        couponRepository.saveAll(List.of(coupon1, coupon2));

        List<CouponEmbeddable> coupons = user.getCoupons();
        coupons.add(CouponEmbeddable.of(coupon1.getId(), coupon1.getName()));
        coupons.add(CouponEmbeddable.of(coupon2.getId(), coupon2.getName()));
        userRepository.save(user);

        // when, then
        assertThatThrownBy(() -> couponService.getUserCoupons(user.getId()+1))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("존재하지 않는 회원입니다.");

    }

    @Test
    @DisplayName("시스템에 남은 쿠폰의 개수가 0일 경우 유저는 쿠폰을 할당받을 수 없다.")
    void allocateCoupon_quantityShortage() {
        // given
        Coupon coupon = Coupon.of(0L, "coupon3", 0.10, LocalDateTime.of(2024, 12, 30, 0, 0));
        couponRepository.save(coupon);

        // when, then
        assertThatThrownBy(() -> couponService.decreaseCoupon(coupon.getId()))
                .isInstanceOf(CouponQuantityShortageException.class)
                .hasMessageContaining("남은 쿠폰의 개수가 요청한 개수보다 적습니다.");
    }

    @Test
    @DisplayName("쿠폰을 유저에게 할당하기 위해 개수를 줄인다.")
    void decreaseCoupon() {
        // given
        Coupon coupon = Coupon.of(10L, "coupon3", 0.10, LocalDateTime.of(2024, 12, 30, 0, 0));
        couponRepository.save(coupon);

        // when
        couponService.decreaseCoupon(coupon.getId());
        Coupon foundCoupon = couponRepository.findById(coupon.getId()).orElseThrow();

        // then
        assertEquals(9L, foundCoupon.getQuantity());
    }

    public interface Callback {
        void allocateCouponToUserWithLock(Long userId, Long couponId);
    }

    public class AllocateCouponTestTemplate {
        public void allocateCouponTo100Thread(Callback callback) throws InterruptedException {
            // given
            User user = createUser("tester");
            userRepository.save(user);

            Coupon coupon = createCoupon("coupon");
            couponRepository.save(coupon);

            // when
            int threadCount = 100;
            ExecutorService executorService = Executors.newFixedThreadPool(32);
            CountDownLatch latch = new CountDownLatch(threadCount);

            for(int i = 0; i < threadCount; i++) {
                executorService.submit(() -> {
                    try {
                        callback.allocateCouponToUserWithLock(user.getId(), coupon.getId());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();

            // then
            Coupon foundCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
            assertEquals(0, foundCoupon.getQuantity());
        }
    }

    @Test
    @DisplayName("100개의 스레드가 비관적 락을 사용해 동시에 쿠폰을 요청한다.")
    public void allocateCouponTo100ThreadWithPessimisticLock() throws InterruptedException {
        AllocateCouponTestTemplate template = new AllocateCouponTestTemplate();
        template.allocateCouponTo100Thread((userId, couponId) -> couponService.allocateCouponToUserWithPessimisticLock(userId, couponId));
    }
    @Test
    @DisplayName("100개의 스레드가 네임드 락을 사용해 동시에 쿠폰을 요청한다.")
    public void allocateCouponTo100ThreadWithNamedLock() throws InterruptedException {
        AllocateCouponTestTemplate template = new AllocateCouponTestTemplate();
        template.allocateCouponTo100Thread((userId, couponId) -> couponService.allocateCouponToUserWithNamedLock(userId, couponId));
    }
    @Test
    @DisplayName("100개의 스레드가 redisson 분산 락을 사용해 동시에 쿠폰을 요청한다.")
    public void allocateCouponTo100ThreadWithDistributedLock() throws InterruptedException {
        AllocateCouponTestTemplate template = new AllocateCouponTestTemplate();
        template.allocateCouponTo100Thread((userId, couponId) -> couponService.allocateCouponToUserWithDistributedLock(userId, couponId));
    }
}