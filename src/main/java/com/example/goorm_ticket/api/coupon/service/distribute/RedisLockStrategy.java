package com.example.goorm_ticket.api.coupon.service.distribute;

import com.example.goorm_ticket.api.coupon.exception.CouponException.CouponNotFoundException;
import com.example.goorm_ticket.api.coupon.exception.CouponException.UserNotFoundException;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.coupon.entity.UserCoupon;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Qualifier("redisLock")
public class RedisLockStrategy{
    protected final CouponRepository couponRepository;
    protected final UserRepository userRepository;

    public RedisLockStrategy(CouponRepository couponRepository, UserRepository userRepository) {
        this.couponRepository = couponRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @DistributedLock(key = "#couponId")
    public void couponDecrease(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(IllegalArgumentException::new);
        coupon.decreaseQuantity(1L);
    }


    //지금 안씀
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

    @DistributedLock(key = "#couponId")
    protected CouponResponseDto decreaseCoupon(Long couponId) {
        System.out.println("decreaseCoupon 실행");

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

    protected User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    protected Coupon findCouponById(Long couponId) {
        return couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponNotFoundException(couponId));
    }
}
