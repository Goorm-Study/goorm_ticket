package com.example.shared.api.coupon.service.strategy;

import com.example.shared.domain.coupon.dto.CouponResponseDto;
import com.example.shared.domain.coupon.entity.Coupon;
import com.example.shared.domain.coupon.repository.CouponRepository;
import com.example.shared.domain.user.entity.User;
import com.example.shared.domain.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

public class NoLockStrategy extends AbstractCouponAllocation {

    public NoLockStrategy(CouponRepository couponRepository, UserRepository userRepository) {
        super(couponRepository, userRepository);
    }

    @Override
    @Transactional
    public CouponResponseDto allocateCoupon(Long userId, Long couponId) {
        User user = findUserById(userId);

        CouponResponseDto couponResponseDto = decreaseCoupon(couponId);

        Coupon coupon = findCouponById(couponId);

        //쿠폰 발급
        user.addCoupon(coupon);
        userRepository.save(user);

        return couponResponseDto;
    }
}
