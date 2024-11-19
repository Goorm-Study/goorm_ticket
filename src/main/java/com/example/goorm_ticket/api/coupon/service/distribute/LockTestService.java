package com.example.goorm_ticket.api.coupon.service.distribute;

import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
