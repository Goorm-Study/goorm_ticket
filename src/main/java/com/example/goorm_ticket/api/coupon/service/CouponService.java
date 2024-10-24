package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.aop.annotation.DistributedLock;
import com.example.goorm_ticket.aop.annotation.NamedLock;
import com.example.goorm_ticket.api.coupon.exception.CouponException;
import com.example.goorm_ticket.domain.coupon.dto.CouponRequestDto;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import jakarta.persistence.OptimisticLockException;
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
    public CouponResponseDto decreaseCouponWithPessimisticLock(Long couponId) {
        Coupon coupon = findCouponByIdWithPessimisticLock(couponId);
        coupon.decreaseQuantity(1L);

        couponRepository.save(coupon);

        return CouponResponseDto.of(coupon.getId(), coupon.getName());
    }

    @Transactional
    public CouponResponseDto allocateCouponToUserWithPessimisticLock(Long userId, Long couponId) {
        User user = findUserById(userId);

        CouponResponseDto couponResponseDto = decreaseCouponWithPessimisticLock(couponId);

        List<CouponEmbeddable> userCoupons = user.getCoupons();
        userCoupons.add(CouponEmbeddable.of(couponResponseDto.getId(), couponResponseDto.getName()));
        userRepository.save(user);

        return CouponResponseDto.of(couponId);
    }

    @NamedLock(lockKey = "'coupon' + #couponId")
    public CouponResponseDto allocateCouponToUserWithNamedLock(Long userId, Long couponId) {
        User user = findUserById(userId);

        CouponResponseDto couponResponseDto = decreaseCoupon(couponId);

        List<CouponEmbeddable> userCoupons = user.getCoupons();
        userCoupons.add(CouponEmbeddable.of(couponResponseDto.getId(), couponResponseDto.getName()));
        userRepository.save(user);

        return CouponResponseDto.of(couponId);
    }

    @DistributedLock(key = "'coupon' + #couponId")
    public CouponResponseDto allocateCouponToUserWithDistributedLock(Long userId, Long couponId) {
        User user = findUserById(userId);

        CouponResponseDto couponResponseDto = decreaseCoupon(couponId);

        List<CouponEmbeddable> userCoupons = user.getCoupons();
        userCoupons.add(CouponEmbeddable.of(couponResponseDto.getId(), couponResponseDto.getName()));
        userRepository.save(user);

        return CouponResponseDto.of(couponId);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Coupon findCouponById(Long couponId) {
        return couponRepository.findById(couponId).orElseThrow(() -> new CouponNotFoundException(couponId));
    }

    public Coupon findCouponByIdWithPessimisticLock(Long couponId) {
        return couponRepository.findByIdWithPessimisticLock(couponId).orElseThrow(() -> new CouponException.CouponNotFoundException(couponId));
    }
}
