package com.example.shared.api.coupon.service.distribute;

import com.example.shared.domain.coupon.entity.Coupon;
import com.example.shared.domain.coupon.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LockTestService {

    private final CouponRepository couponRepository;

    public LockTestService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Transactional
    @DistributedLock(key = "#couponId")
    public String decreaseCouponQuantity(Long couponId, Long quantity) {
        Coupon coupon = couponRepository.findByIdWithLock(couponId)
                .orElseThrow(() -> new IllegalArgumentException("Coupon not found with id: " + couponId));

        coupon.decreaseQuantity(quantity);

        couponRepository.saveAndFlush(coupon);

        System.out.println("쿠폰 감소 비즈니스 로직 실행: " + couponId);
        return "success";
    }

}
