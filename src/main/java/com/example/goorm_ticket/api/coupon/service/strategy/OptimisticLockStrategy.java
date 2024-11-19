package com.example.goorm_ticket.api.coupon.service.strategy;

import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OptimisticLockStrategy extends AbstractCouponAllocation{

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

        // 유저 쿠폰 리스트에 추가
        List<CouponEmbeddable> userCoupons = user.getCoupons();
        userCoupons.add(CouponEmbeddable.of(couponResponseDto.getId(), couponResponseDto.getName()));
        userRepository.save(user);

        return couponResponseDto;
    }
}
