package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.api.coupon.exception.CouponException;
import com.example.goorm_ticket.domain.coupon.dto.CouponRequestDto;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.coupon.entity.CouponIssuance;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.goorm_ticket.api.coupon.exception.CouponException.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CouponService {
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CouponResponseDto> getAllCoupons() {
        List<Coupon> couponList = couponRepository.findAll();
        return couponList.stream()
                .map(coupon -> CouponResponseDto.of(
                        coupon.getId(),
                        coupon.getQuantity(),
                        coupon.getName(),
                        coupon.getDiscountRate(),
                        coupon.getExpirationDate())
                )
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CouponResponseDto> getUserCoupons(Long userId) {
        User user = findUserById(userId);
        return user.getCoupons().stream()
                .map(coupon -> CouponResponseDto.of(coupon.getId(), coupon.getName()))
                .collect(Collectors.toList());
    }

    @Transactional
    public CouponResponseDto allocateCouponToUser(Long userId, Long couponId) {
        User user = findUserById(userId);

        boolean success = decreaseCouponWithLock(couponId);
        // 쿠폰 감소 로직이 성공하면 그 쿠폰을 유저에게 할당
        if (success) {
            List<CouponEmbeddable> userCoupons = user.getCoupons();
            Coupon coupon = findCouponById(couponId);
            String couponName = coupon.getName();

            userCoupons.add(CouponEmbeddable.of(couponId, couponName));
        }

        return CouponResponseDto.of(couponId, success ? CouponIssuance.SUCCESS : CouponIssuance.FAILED);
    }

    @Transactional
    public boolean decreaseCouponWithLock(Long couponId) {
        Coupon coupon = findCouponByIdWithLock(couponId);

        if (coupon.getQuantity() > 0) {
            coupon.decreaseQuantity(1L);
            return true;
        }
        return false;
    }

    private Coupon findCouponByIdWithLock(Long couponId) {
        return couponRepository.findByIdWithLock(couponId)
            .orElseThrow(() -> new CouponNotFoundException(couponId));
    }

    public boolean decreaseCoupon(Long couponId) {
        Coupon coupon = findCouponById(couponId);
        coupon.decreaseQuantity(1L);

        couponRepository.save(coupon);

        return true;
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }


    private Coupon findCouponById(Long couponId) {
        return couponRepository.findById(couponId).orElseThrow(() -> new CouponNotFoundException(couponId));
    }
}
