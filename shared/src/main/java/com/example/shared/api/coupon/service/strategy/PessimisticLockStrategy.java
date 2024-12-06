package com.example.shared.api.coupon.service.strategy;

import com.example.shared.api.coupon.exception.CouponException.CouponNotFoundException;
import com.example.shared.domain.coupon.dto.CouponResponseDto;
import com.example.shared.domain.coupon.entity.Coupon;
import com.example.shared.domain.coupon.repository.CouponRepository;
import com.example.shared.domain.user.entity.User;
import com.example.shared.domain.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

public class PessimisticLockStrategy extends AbstractCouponAllocation {

    public PessimisticLockStrategy(CouponRepository couponRepository, UserRepository userRepository) {
        super(couponRepository, userRepository);
    }

    @Override
    @Transactional
    public CouponResponseDto allocateCoupon(Long userId, Long couponId) {
        User user = findUserById(userId);

        // 비관적 락을 사용하여 쿠폰을 가져옴
        Coupon coupon = couponRepository.findByIdWithLock(couponId)
                .orElseThrow(() -> new CouponNotFoundException(couponId));

        CouponResponseDto couponResponseDto = decreaseCoupon(couponId);

        //쿠폰 발급
        user.addCoupon(coupon);
        userRepository.save(user);

        return couponResponseDto;

    }
}
