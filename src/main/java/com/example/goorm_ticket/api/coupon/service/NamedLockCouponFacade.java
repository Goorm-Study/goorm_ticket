package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.lockdomain.LockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NamedLockCouponFacade {
    private final LockRepository lockRepository;
    private final CouponService couponService;

    @Transactional("lockTransactionManager")
    public CouponResponseDto allocateCouponToUserWithNamedLock(Long userId, Long couponId) {
        try {
            lockRepository.getLock(couponId.toString());
            return couponService.allocateCouponToUser(userId, couponId);
        } finally {
            lockRepository.releaseLock(couponId.toString());
        }
    }
}
