package org.example.consumer.service;

import com.example.shared.domain.coupon.entity.Coupon;
import com.example.shared.domain.coupon.repository.CouponCountRepository;
import com.example.shared.domain.coupon.repository.CouponRepository;
import com.example.shared.domain.user.entity.User;
import com.example.shared.domain.user.repository.UserRepository;
import org.example.consumer.consumer.CouponCreatedConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KafkaCouponService {

    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final CouponCountRepository couponCountRepository;

    private final Logger logger = LoggerFactory.getLogger(KafkaCouponService.class);

    public KafkaCouponService(UserRepository userRepository, CouponRepository couponRepository,
                              CouponCountRepository couponCountRepository) {
        this.userRepository = userRepository;
        this.couponRepository = couponRepository;
        this.couponCountRepository = couponCountRepository;
    }

    @Transactional
    public void allocate(Long userId, Long couponId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new RuntimeException("쿠폰을 찾을 수 없습니다."));

        String couponKey = "coupon" + couponId;
        Long issuedCount = couponCountRepository.getIssuedCount(couponKey); // 현재 발급 수량
        Long maxQuantity = couponCountRepository.getCouponQuantity(couponId);

        if (issuedCount >= maxQuantity) {
            logger.info("쿠폰 발급 초과 - userId={}, couponId={}", userId, couponId);
            return;
        }

        //쿠폰 발급
        user.addCoupon(coupon);
        couponCountRepository.increment(couponKey);

        userRepository.save(user);
        logger.info("쿠폰 발급 성공 - userId={}, couponId={}", userId, couponId);
    }
}
