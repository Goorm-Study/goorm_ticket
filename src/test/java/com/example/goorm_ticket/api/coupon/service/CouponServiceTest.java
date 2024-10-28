package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.api.coupon.exception.CouponException;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {
    @Mock
    private CouponRepository couponRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CouponService couponService;

    @Test
    @DisplayName("쿠폰 2개를 넣은 후 전체 쿠폰 목록을 조회한다.")
    void getAllCoupons() {
        // given
        Coupon coupon1 = createCoupon("coupon1");
        Coupon coupon2 = createCoupon("coupon2");

        when(couponRepository.findAll()).thenReturn(List.of(coupon1, coupon2));

        // when
        List<CouponResponseDto> couponList = couponService.getAllCoupons();

        // then
        assertThat(couponList).hasSize(2);
        assertThat(couponList.get(0).getId()).isEqualTo(coupon1.getId());
        assertThat(couponList.get(1).getId()).isEqualTo(coupon2.getId());
    }
    @Test
    @DisplayName("유저 쿠폰 목록에 쿠폰을 2개 넣은 후 유저 쿠폰 목록을 조회한다.")
    void getUserCoupons() {
        // given
        User user = createUser("tester");

        Coupon coupon1 = createCoupon("coupon1");
        Coupon coupon2 = createCoupon("coupon2");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        List<CouponEmbeddable> coupons = user.getCoupons();
        coupons.add(CouponEmbeddable.of(coupon1.getId(), coupon1.getName()));
        coupons.add(CouponEmbeddable.of(coupon2.getId(), coupon2.getName()));

        // when
        List<CouponResponseDto> userCoupons = couponService.getUserCoupons(1L);

        // then
        assertThat(userCoupons).hasSize(2);
        assertThat(userCoupons.get(0).getId()).isEqualTo(coupon1.getId());
        assertThat(userCoupons.get(1).getId()).isEqualTo(coupon2.getId());
    }

    @Test
    @DisplayName("존재하지 않는 유저의 경우 유저의 쿠폰 목록을 확인할 수 없다.")
    void getUserCoupons_userNotFound() {
        // given
        User user = createUser("tester");

        Coupon coupon1 = createCoupon("coupon1");
        Coupon coupon2 = createCoupon("coupon2");

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        List<CouponEmbeddable> coupons = user.getCoupons();
        coupons.add(CouponEmbeddable.of(coupon1.getId(), coupon1.getName()));
        coupons.add(CouponEmbeddable.of(coupon2.getId(), coupon2.getName()));

        // when, then
        assertThatThrownBy(() -> couponService.getUserCoupons(2L))
                .isInstanceOf(CouponException.UserNotFoundException.class)
                .hasMessageContaining("존재하지 않는 회원입니다.");
    }

    @Test
    @DisplayName("시스템에 남은 쿠폰의 개수가 0일 경우 유저는 쿠폰을 할당받을 수 없다.")
    void allocateCoupon_quantityShortage() {
        // given
        Coupon coupon = Coupon.of(0L, "coupon3", 0.10, LocalDateTime.of(2024, 12, 30, 0, 0));
        when(couponRepository.findById(anyLong())).thenReturn(Optional.of(coupon));
        // when, then
        assertThatThrownBy(() -> couponService.decreaseCoupon(1L))
                .isInstanceOf(CouponException.CouponQuantityShortageException.class)
                .hasMessageContaining("남은 쿠폰의 개수가 요청한 개수보다 적습니다.");
    }

    @Test
    @DisplayName("쿠폰을 유저에게 할당하기 위해 개수를 줄인다.")
    void decreaseCoupon() {
        // given
        Coupon coupon = Coupon.of(10L, "coupon3", 0.10, LocalDateTime.of(2024, 12, 30, 0, 0));
        when(couponRepository.findById(anyLong())).thenReturn(Optional.of(coupon));

        // when
        couponService.decreaseCoupon(1L);

        // then
        assertEquals(9L, coupon.getQuantity());
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


}
