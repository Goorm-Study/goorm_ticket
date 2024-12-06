package com.example.shared.api.coupon.service.kafka;

import com.example.shared.api.order.exception.BusinessException.CouponNotFoundException;
import com.example.shared.domain.coupon.entity.Coupon;
import com.example.shared.domain.coupon.repository.CouponCountRepository;
import com.example.shared.domain.coupon.repository.CouponRepository;
import com.example.shared.producer.CouponCreateProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaAllocationService {
    private final CouponCountRepository couponCountRepository;
    private final CouponCreateProducer couponCreateProducer;

    public void allocateCoupon(Long userId, Long couponId) {
        String couponKey = "coupon:" + couponId;
        Long issuedCount= couponCountRepository.getIssuedCount(couponKey);

        System.out.println("issuedCount = " + issuedCount);

        if (issuedCount >= couponCountRepository.getCouponQuantity(couponId)) {
            throw new RuntimeException("쿠폰이 모두 소진되었습니다.");
        }

        couponCreateProducer.issue(userId, couponId);
    }

}
