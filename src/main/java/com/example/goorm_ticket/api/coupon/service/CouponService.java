package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.domain.coupon.dto.CouponResponse;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    public List<CouponResponse> getAllCoupons() {
        List<Coupon> couponList = couponRepository.findAll();
        return couponList.stream()
                .map(coupon -> CouponResponse.builder()
                        .id(coupon.getId())
                        .name(coupon.getName())
                        .quantity(coupon.getQuantity())
                        .discountRate(coupon.getDiscountRate())
                        .expirationDate(coupon.getExpirationDate())
                        .build()
                )
                .collect(Collectors.toList());
    }

    public List<CouponResponse> getUserCoupons(Long user_id) {
        User user = userRepository.findById(user_id).orElseThrow();
        return user.getCoupons().stream()
                .map(coupon -> CouponResponse.builder()
                        .id(coupon.getId())
                        .name(coupon.getName())
                        .build()
                )
                .collect(Collectors.toList());
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean decreaseCoupon(Long coupon_id) {
        Coupon coupon = couponRepository.findById(coupon_id).orElseThrow();
        coupon.decreaseQuantity(1L);
        couponRepository.save(coupon);

        return true;
    }
}
