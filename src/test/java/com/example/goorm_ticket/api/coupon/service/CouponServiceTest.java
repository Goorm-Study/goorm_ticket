package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.domain.coupon.dto.CouponResponse;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CouponServiceTest {
    @Autowired
    private CouponService couponService;

    @Autowired
    private UserCouponService userCouponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserRepository userRepository;

    private Long user_id;
    private Long coupon1_id;
    private Long coupon2_id;

    @BeforeEach
    void before() {
        Coupon coupon1 = Coupon.builder()
                .quantity(100L)
                .name("coupon1")
                .discountRate(0.15)
                .expirationDate(LocalDateTime.of(2024, 9, 30, 0, 0))
                .build();
        Coupon coupon2 = Coupon.builder()
                .quantity(15L)
                .name("coupon2")
                .discountRate(0.20)
                .expirationDate(LocalDateTime.of(2024, 9, 30, 0, 0))
                .build();

        couponRepository.saveAndFlush(coupon1);
        couponRepository.saveAndFlush(coupon2);

        coupon1_id = coupon1.getId();
        coupon2_id = coupon2.getId();
//        System.out.println("coupon1Id = " + coupon1.getId());
//        System.out.println("coupon2Id = " + coupon2.getId());

        User user = User.builder()
                .username("tester")
                .password("1234")
                .build();

        userRepository.saveAndFlush(user);
        user_id = user.getId();

        List<CouponEmbeddable> coupons = user.getCoupons();
        coupons.add(CouponEmbeddable.builder()
                .id(coupon1_id)
                .name(coupon1.getName())
                .build());
        coupons.add(CouponEmbeddable.builder()
                .id(coupon2_id)
                .name(coupon2.getName())
                .build());

        userRepository.saveAndFlush(user);
    }


    @AfterEach
    public void after() {
        couponRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void 쿠폰_전체_조회() {
        List<CouponResponse> couponList = couponService.getAllCoupons();
        assertEquals(2, couponList.size());
    }

    @Test
    @Transactional
    void 유저_쿠폰_조회() {
        List<CouponResponse> userCoupons = couponService.getUserCoupons(user_id);
        Long couponId1 = userCoupons.get(0).getId();
        Long couponId2 = userCoupons.get(1).getId();

        assertEquals(coupon1_id, couponId1);
        assertEquals(coupon2_id, couponId2);
    }

    @Test
    void 유저_쿠폰_할당() {
        Coupon coupon3 = Coupon.builder()
                .quantity(10L)
                .name("coupon3")
                .discountRate(0.10)
                .expirationDate(LocalDateTime.of(2024, 9, 30, 0, 0))
                .build();
        couponRepository.saveAndFlush(coupon3);

        Long coupon3_id = coupon3.getId();
        couponService.decreaseCoupon(coupon3_id);
        Coupon coupon = couponRepository.findById(coupon3_id).orElseThrow();
        assertEquals(9L, coupon.getQuantity());
        // 커밋을 해줘야 db에 쿠폰이 들어가므로 이 방법으론 테스트 안됨
//        CouponResponse couponResponse = userCouponService.allocateCouponToUser(user_id, coupon3_id);
//        assertEquals(coupon3_id, couponResponse.getId());
//        Coupon coupon = couponRepository.findById(coupon3_id).orElseThrow();
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