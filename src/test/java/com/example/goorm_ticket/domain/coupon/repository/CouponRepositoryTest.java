package com.example.goorm_ticket.domain.coupon.repository;

import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CouponRepositoryTest {
    @Autowired
    CouponRepository couponRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("쿠폰을 저장한다.")
    public void saveCoupon() {
        // given, when
        Coupon coupon = Coupon.of(100L,
                "coupon",
                0.15,
                LocalDateTime.of(2024, 12, 30, 0, 0)
        );

        Coupon savedCoupon = couponRepository.save(coupon);

        // then
        assertNotNull(savedCoupon);
        assertEquals(coupon.getName(), savedCoupon.getName());
        assertEquals(coupon.getDiscountRate(), savedCoupon.getDiscountRate());
        assertEquals(coupon.getExpirationDate(), savedCoupon.getExpirationDate());
    }

    @Test
    @DisplayName("쿠폰을 조회한다.")
    public void findCouponById() {
        // given
        Coupon coupon = Coupon.of(100L,
                "coupon",
                0.15,
                LocalDateTime.of(2024, 12, 30, 0, 0)
        );

        couponRepository.save(coupon);

        // when
        Optional<Coupon> foundCoupon = couponRepository.findById(coupon.getId());

        // then
        assertTrue(foundCoupon.isPresent());
        assertEquals(coupon.getId(), foundCoupon.get().getId());
    }

    @Test
    @DisplayName("쿠폰을 삭제한다.")
    public void deleteCoupon() {
        // given
        Coupon coupon = Coupon.of(100L,
                "coupon",
                0.15,
                LocalDateTime.of(2024, 12, 30, 0, 0)
        );
        Coupon savedCoupon = couponRepository.save(coupon);

        // when
        couponRepository.delete(savedCoupon);
        Optional<Coupon> deletedCoupon = couponRepository.findById(savedCoupon.getId());

        // then
        assertTrue(deletedCoupon.isEmpty());
    }

    @Test
    @DisplayName("사용자의 쿠폰 리스트에 쿠폰을 추가한다.")
    public void saveUserWithCoupons() {
        // given
        User user = User.builder()
                .username("tester")
                .password("1234")
                .build();
        User savedUser = userRepository.save(user);

        Coupon coupon = Coupon.of(100L,
                "coupon",
                0.15,
                LocalDateTime.of(2024, 12, 30, 0, 0)
        );
        Coupon savedCoupon = couponRepository.save(coupon);

        List<CouponEmbeddable> userCoupons = savedUser.getCoupons();
        userCoupons.add(CouponEmbeddable.of(savedCoupon.getId(), savedCoupon.getName()));
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertEquals(1, foundUser.get().getCoupons().size());
        assertEquals("coupon", foundUser.get().getCoupons().get(0).getName());
    }

}