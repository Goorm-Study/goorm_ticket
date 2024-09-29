package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.api.coupon.exception.CouponException;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.goorm_ticket.api.coupon.exception.CouponException.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CouponServiceTest {
    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserRepository userRepository;

    private Long userId;
    private Long coupon1Id;
    private Long coupon2Id;

    @BeforeEach
    void before() {
        Coupon coupon1 = Coupon.of(100L,
                        "coupon1",
                        0.15,
                        LocalDateTime.of(2024, 12, 30, 0, 0)
                        );

        Coupon coupon2 = Coupon.of(15L,
                "coupon2",
                0.20,
                LocalDateTime.of(2024, 12, 30, 0, 0)
        );

        couponRepository.saveAndFlush(coupon1);
        couponRepository.saveAndFlush(coupon2);

        coupon1Id = coupon1.getId();
        coupon2Id = coupon2.getId();

        User user = User.builder()
                .username("tester")
                .password("1234")
                .build();

        userRepository.saveAndFlush(user);
        userId = user.getId();

        List<CouponEmbeddable> coupons = user.getCoupons();
        coupons.add(CouponEmbeddable.of(coupon1Id, coupon1.getName()));
        coupons.add(CouponEmbeddable.of(coupon2Id, coupon2.getName()));

        userRepository.saveAndFlush(user);
    }

    @AfterEach
    public void after() {
        couponRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    void 쿠폰_전체_조회() {
        List<CouponResponseDto> couponList = couponService.getAllCoupons();
        assertEquals(2, couponList.size());
    }

    @Test
    @Transactional
    void 유저_쿠폰_조회() {
        // given
        List<CouponResponseDto> userCoupons = couponService.getUserCoupons(userId);

        assertThat(userCoupons).hasSize(2);
    }

    @Test
    void 쿠폰_할당_실패() {
        // given
        Coupon coupon3 = Coupon.of(0L, "coupon3", 0.10, LocalDateTime.of(2024, 12, 30, 0, 0));
        couponRepository.saveAndFlush(coupon3);

        // when, then
        Long coupon3Id = coupon3.getId();
        assertThatThrownBy(() -> couponService.decreaseCoupon(coupon3Id))
                .isInstanceOf(CouponException.CouponQuantityShortageException.class)
                .hasMessageContaining("남은 쿠폰의 개수가 요청한 개수보다 적습니다.");
    }

    @Test
    void 유저_쿠폰_할당() {
        // given
        Coupon coupon3 = Coupon.of(10L, "coupon3", 0.10, LocalDateTime.of(2024, 12, 30, 0, 0));
        couponRepository.saveAndFlush(coupon3);

        // when
        Long coupon3Id = coupon3.getId();
        couponService.decreaseCoupon(coupon3Id);

        // then
        Coupon coupon = couponRepository.findById(coupon3Id).orElseThrow();
        assertEquals(9L, coupon.getQuantity());

        // 커밋을 해줘야 db에 쿠폰이 들어가므로 이 방법으론 테스트 안됨
//        CouponResponse couponResponse = userCouponService.allocateCouponToUser(user_id, coupon3Id);
//        assertEquals(coupon3Id, couponResponse.getId());
//        Coupon coupon = couponRepository.findById(coupon3Id).orElseThrow();
//        assertEquals(9L, coupon.getQuantity());
//        User user = userRepository.findById(user_id).orElseThrow();
//        assertEquals(3, user.getCoupons().size());
    }
}


//    @Test // 아직 동시성 처리 안해서 테스트 실패함
//    @Transactional
//    void 동시에_쿠폰_100개_할당() throws InterruptedException {
//        int threadCount = 100;
//        ExecutorService executorService = Executors.newFixedThreadPool(32);
//        CountDownLatch latch = new CountDownLatch(threadCount);
//
//        for(int i = 0; i < threadCount; i++) {
//            executorService.submit(() -> {
//                try {
//                    userCouponService.allocateCouponToUser(user_id, 1L);
//                } finally {
//                    latch.countDown();
//                }
//            });
//        }
//
//        latch.await();
//
//        Coupon coupon = couponRepository.findById(1L).orElseThrow();
//        assertEquals(0, coupon.getQuantity());
//    }
//}