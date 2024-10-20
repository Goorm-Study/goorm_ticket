package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.api.coupon.exception.CouponException;
import com.example.goorm_ticket.domain.coupon.dto.CouponRequestDto;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

        return CouponResponseDto.of(
                coupon.getId(),
                coupon.getName(),
                coupon.getQuantity(),
                "쿠폰 발급 성공"
        );
    }

    @Transactional
    public CouponResponseDto allocateCouponToUser(Long userId, Long couponId) {
        User user = findUserById(userId);

        CouponResponseDto couponResponseDto = decreaseCoupon(couponId);

        List<CouponEmbeddable> userCoupons = user.getCoupons();
        userCoupons.add(CouponEmbeddable.of(couponResponseDto.getId(), couponResponseDto.getName()));
        userRepository.save(user);

        return couponResponseDto;
    }

    @Transactional
    public CouponResponseDto allocateCouponToUserWithPessimisticLock(Long userId, Long couponId) {
        User user = findUserById(userId);

        // 비관적 락을 사용하여 쿠폰을 가져옴
        Coupon coupon = couponRepository.findByIdWithLock(couponId)
                .orElseThrow(() -> new CouponNotFoundException(couponId));

        CouponResponseDto couponResponseDto = decreaseCoupon(couponId);

        List<CouponEmbeddable> userCoupons = user.getCoupons();
        userCoupons.add(CouponEmbeddable.of(couponResponseDto.getId(), couponResponseDto.getName()));
        userRepository.save(user);

        return couponResponseDto;

    }

    //낙관적 락
    @Retryable(
            value = {ObjectOptimisticLockingFailureException.class},
            maxAttempts = 100,
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public CouponResponseDto allocateCouponToUserWithOptimisticLock(Long userId, Long couponId) {
        User user = findUserById(userId);

        // 쿠폰 수량 감소 처리
        CouponResponseDto couponResponseDto = decreaseCoupon(couponId);

        // 유저 쿠폰 리스트에 추가
        List<CouponEmbeddable> userCoupons = user.getCoupons();
        userCoupons.add(CouponEmbeddable.of(couponResponseDto.getId(), couponResponseDto.getName()));
        userRepository.save(user);

        return couponResponseDto;
    }

    //분산 락
    @Autowired
    private RedissonClient redissonClient;

    @Transactional
    public CouponResponseDto allocateCouponToUserWithRedis(Long userId, Long couponId) {
        // RLock 객체 생성
        RLock lock = redissonClient.getLock("couponLock:" + couponId);

        try {
            // 락을 10초 동안 대기하고, 1분 동안 락을 유지
            if (lock.tryLock(30, 60, TimeUnit.SECONDS)) {
                try {
                    // 락이 성공적으로 획득되었을 때 실행할 로직
                    System.out.println("락 획득: " + LocalDateTime.now());

                    User user = findUserById(userId);

                    CouponResponseDto couponResponseDto = decreaseCoupon(couponId);

                    List<CouponEmbeddable> userCoupons = user.getCoupons();
                    userCoupons.add(CouponEmbeddable.of(couponResponseDto.getId(), couponResponseDto.getName()));
                    userRepository.save(user);

                    return couponResponseDto;
                } finally {
                    if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                            @Override
                            public void afterCompletion(int status) {
                                lock.unlock();
                            }
                        });
                    }
                }
            } else {
                throw new RuntimeException("쿠폰을 발급하는데 실패했습니다. 다른 사용자가 쿠폰을 발급 중입니다.");
            }
        } catch (Exception e) {
            throw new RuntimeException("쿠폰 발급 중 오류가 발생했습니다.", e);
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }


    private Coupon findCouponById(Long couponId) {
        return couponRepository.findById(couponId).orElseThrow(() -> new CouponNotFoundException(couponId));
    }
}
