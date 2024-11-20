package com.example.goorm_ticket.api.coupon.service.strategy;

import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public class NoLockStrategy extends AbstractCouponAllocation{

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
