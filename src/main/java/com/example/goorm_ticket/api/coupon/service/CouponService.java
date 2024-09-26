package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.domain.coupon.dto.CouponResponse;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;

    public List<CouponResponse> getAllCoupons() {
        List<Coupon> couponList = couponRepository.findAll();
        return couponList.stream()
                .map(coupon -> new CouponResponse.builder()
                        .id(coupon.getId())
                        .name(coupon.getName())
                        .quantity(coupon.getQuantity())
                        .discountRate(coupon.getDiscountRate())
                        .expirationDate(coupon.getExpirationDate()))
                .collect(Collectors.toList());
    }


    @Transactional
    public boolean decreaseCoupon(Long id, Long quantity) {
        Coupon coupon = couponRepository.findById(id).orElseThrow();
        coupon.decreaseQuantity(quantity);
        couponRepository.save(coupon);

        return true;
    }

    // 유저 가져와서 decreaseCoupon 하고 유저 쿠폰에 넣어줘야 할듯
}
