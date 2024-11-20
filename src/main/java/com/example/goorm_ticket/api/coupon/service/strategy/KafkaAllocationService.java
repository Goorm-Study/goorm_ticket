package com.example.goorm_ticket.api.coupon.service.strategy;

import com.example.goorm_ticket.api.coupon.exception.CouponException.CouponNotFoundException;
import com.example.goorm_ticket.api.coupon.exception.CouponException.UserNotFoundException;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.coupon.repository.CouponCountRepository;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import com.example.goorm_ticket.producer.CouponCreateProducer;
import java.util.List;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KafkaAllocationService {
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final CouponCountRepository couponCountRepository;
    private final CouponCreateProducer couponCreateProducer;


    public KafkaAllocationService(CouponRepository couponRepository, UserRepository userRepository,
                                  CouponCountRepository couponCountRepository,
                                  CouponCreateProducer couponCreateProducer) {
        this.couponRepository = couponRepository;
        this.userRepository = userRepository;
        this.couponCountRepository = couponCountRepository;
        this.couponCreateProducer = couponCreateProducer;
    }

    @Transactional
    public CouponResponseDto decreaseCoupon(Long couponId) {
        Coupon coupon = findCouponById(couponId);
        coupon.decreaseQuantity(1L);

        couponRepository.save(coupon);

        return CouponResponseDto.of(
                coupon.getId(),
                coupon.getName(),
                coupon.getQuantity(),
                "쿠폰 발급 성공"
        );
    }


    public void allocateCoupon(Long userId, Long couponId) {
        String couponKey = "coupon:" + couponId;
        Long issuedCount= couponCountRepository.increment(couponKey);

        Coupon coupon = findCouponById(couponId);
        if (issuedCount > coupon.getQuantity()) {
            throw new RuntimeException("쿠폰이 모두 소진되었습니다.");
        }

        couponCreateProducer.issue(userId, couponId);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Coupon findCouponById(Long couponId) {
        return couponRepository.findById(couponId).orElseThrow(() -> new CouponNotFoundException(couponId));
    }
}
