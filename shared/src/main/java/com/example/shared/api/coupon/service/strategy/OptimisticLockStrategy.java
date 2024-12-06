package com.example.shared.api.coupon.service.strategy;

import com.example.shared.domain.coupon.dto.CouponResponseDto;
import com.example.shared.domain.coupon.entity.Coupon;
import com.example.shared.domain.coupon.repository.CouponRepository;
import com.example.shared.domain.user.entity.User;
import com.example.shared.domain.user.repository.UserRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;

public class OptimisticLockStrategy extends AbstractCouponAllocation {

    public OptimisticLockStrategy(CouponRepository couponRepository, UserRepository userRepository) {
        super(couponRepository, userRepository);
    }

    //낙관적 락
    @Override
    @Retryable(
            value = {ObjectOptimisticLockingFailureException.class},
            maxAttempts = 100,
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public CouponResponseDto allocateCoupon(Long userId, Long couponId) {
        User user = findUserById(userId);

        // 쿠폰 수량 감소 처리
        CouponResponseDto couponResponseDto = decreaseCoupon(couponId);

        Coupon coupon = findCouponById(couponId);

        //쿠폰 발급
        user.addCoupon(coupon);
        userRepository.save(user);

        return couponResponseDto;
    }
}
