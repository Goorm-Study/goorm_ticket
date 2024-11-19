package com.example.goorm_ticket.api.coupon.service.strategy;

import com.example.goorm_ticket.api.coupon.exception.CouponException.CouponNotFoundException;
import com.example.goorm_ticket.api.coupon.exception.CouponException.UserNotFoundException;
import com.example.goorm_ticket.api.coupon.service.distribute.DistributedLock;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

public abstract class AbstractCouponAllocation implements CouponAllocationStrategy{
    protected final CouponRepository couponRepository;
    protected final UserRepository userRepository;

    public AbstractCouponAllocation(CouponRepository couponRepository, UserRepository userRepository) {
        this.couponRepository = couponRepository;
        this.userRepository = userRepository;
    }

    protected User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    protected Coupon findCouponById(Long couponId) {
        return couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFoundException(couponId));
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
}
