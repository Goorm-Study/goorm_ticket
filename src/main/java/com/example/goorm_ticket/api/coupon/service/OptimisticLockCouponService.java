package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.api.coupon.exception.CouponException;
import com.example.goorm_ticket.api.user.service.UserService;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@Service
public class OptimisticLockCouponService {
    private final CouponService couponService;

//    @Transactional
//    public CouponResponseDto allocateCouponToUserWithOptimisticLock2(Long userId, Long couponId) {
//        User user = findUserById(userId);
//        CouponResponseDto couponResponseDto = null;
//        while(true) {
//            try {
//                couponResponseDto = couponService.decreaseCoupon(couponId);
//                break;
//            } catch(Exception e) {
//                sleep(50);
//            }
//        }
//
//        List<CouponEmbeddable> userCoupons = user.getCoupons();
//
//        userCoupons.add(CouponEmbeddable.of(couponResponseDto.getId(), couponResponseDto.getName()));
//        userRepository.save(user);
//
//        return CouponResponseDto.of(couponId);
//    }

    public CouponResponseDto allocateCouponToUserWithOptimisticLock(Long userId, Long couponId) {
        while(true) {
            try {
                return couponService.allocateCouponToUser(userId, couponId);
            } catch(Exception e) {
                if (e instanceof ObjectOptimisticLockingFailureException) {
                    sleep(50);  // 낙관적 락에 의한 예외 발생 시 재시도
                } else {
                    throw e;
                }
            }
        }
    }

    public void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
