package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.api.coupon.exception.CouponException;
import com.example.goorm_ticket.domain.coupon.dto.CouponRequestDto;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.goorm_ticket.api.coupon.exception.CouponException.*;

@Service
@RequiredArgsConstructor
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
    public CouponResponseDto decreaseCoupon(Long couponId) {
        Coupon coupon = findCouponById(couponId);
        coupon.decreaseQuantity(1L);

        couponRepository.save(coupon);

        return CouponResponseDto.of(coupon.getId(), coupon.getName());
    }

    @Transactional
    public CouponResponseDto allocateCouponToUser(Long userId, Long couponId) {
        User user = findUserById(userId);
        CouponResponseDto couponResponseDto = decreaseCoupon(couponId);

        try {
            // 쿠폰 감소 로직이 성공하면 그 쿠폰을 유저에게 할당
            couponResponseDto = decreaseCoupon(couponId);
            List<CouponEmbeddable> userCoupons = user.getCoupons();
            userCoupons.add(CouponEmbeddable.of(couponResponseDto.getId(), couponResponseDto.getName()));
            userRepository.save(user);

            // 성공 메시지 반환
            return CouponResponseDto.of(couponId, couponResponseDto.getName(), "쿠폰 발급 성공");
        } catch (CouponException e) {
            // 실패 시 실패 메시지와 함께 예외를 다시 던짐
            throw new CouponException(e.getMessage(), e.getErrorCode());
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }


    private Coupon findCouponById(Long couponId) {
        return couponRepository.findById(couponId).orElseThrow(() -> new CouponNotFoundException(couponId));
    }
}
