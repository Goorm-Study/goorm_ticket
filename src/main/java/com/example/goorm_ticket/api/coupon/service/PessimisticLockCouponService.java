package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.api.coupon.exception.CouponException;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PessimisticLockCouponService {
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    @Transactional
    public CouponResponseDto decreaseCoupon(Long couponId) {
        Coupon coupon = findCouponByIdWithPessimisticLock(couponId);
        coupon.decreaseQuantity(1L);

        couponRepository.save(coupon);

        return CouponResponseDto.of(coupon.getId(), coupon.getName());
    }
    @Transactional
    public CouponResponseDto allocateCouponToUserWithPessimisticLock(Long userId, Long couponId) {
        User user = findUserById(userId);

        CouponResponseDto couponResponseDto = decreaseCoupon(couponId);

        // 이 로직도 User 엔티티에 정의해서 user에 쿠폰을 추가해달라 요청하는 식으로 메소드룰 호출하는게 맞는건가..?
        List<CouponEmbeddable> userCoupons = user.getCoupons();
        userCoupons.add(CouponEmbeddable.of(couponResponseDto.getId(), couponResponseDto.getName()));
        userRepository.save(user);

        return CouponResponseDto.of(couponId);
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CouponException.UserNotFoundException(userId));
    }


    public Coupon findCouponByIdWithPessimisticLock(Long couponId) {
        return couponRepository.findByIdWithPessimisticLock(couponId).orElseThrow(() -> new CouponException.CouponNotFoundException(couponId));
    }
}
