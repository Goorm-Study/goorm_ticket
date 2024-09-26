package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.domain.coupon.dto.CouponResponse;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserCouponService {
    private final UserRepository userRepository;
    private final CouponService couponService;
    private final CouponRepository couponRepository;


    //  CouponService의 decrease()와 allocateCouponToUser()의 클래스를 분리해야 프록시 분리가 되서 트랜잭션이 분리됨
    @Transactional
    public CouponResponse allocateCouponToUser(Long user_id, Long coupon_id) {
        User user = userRepository.findById(user_id).orElseThrow();
        try {
            boolean success = couponService.decreaseCoupon(coupon_id);
            // 쿠폰 감소 로직이 성공하면 그 쿠폰을 유저에게 할당
            if (success) {
                List<CouponEmbeddable> userCoupons = user.getCoupons();
                Coupon coupon = couponRepository.findById(coupon_id).orElseThrow();
                String coupon_name = coupon.getName();

                userCoupons.add(CouponEmbeddable.builder()
                        .id(coupon_id)
                        .name(coupon_name)
                        .build());

                userRepository.save(user);
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        return CouponResponse.builder()
                .id(coupon_id)
                .build();
    }
}
