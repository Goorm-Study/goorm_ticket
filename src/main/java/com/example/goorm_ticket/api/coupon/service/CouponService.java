
package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.api.coupon.exception.CouponException;
import com.example.goorm_ticket.api.coupon.redis.RedisTransaction;
import com.example.goorm_ticket.api.coupon.redis.TimeAttackOperation;
import com.example.goorm_ticket.api.coupon.redis.TimeAttackVO;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTransaction<TimeAttackVO> redisTransaction;
    private final TimeAttackOperation timeAttackOperation;

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

    public CouponResponseDto allocateCouponToUser(Long userId, Long couponId) {
        User user = findUserById(userId);
        TimeAttackVO vo = new TimeAttackVO(userId, couponId);

        // Redis 트랜잭션 실행하여 결과를 리스트로 받기
        Object transactionResult = redisTransaction.execute(redisTemplate, timeAttackOperation, vo);

        // 디버그 로그 추가
        log.debug("Transaction result: {}", transactionResult);

        // transactionResult가 List인지 확인하고, 첫 번째 요소가 Long인지 확인
        if (transactionResult instanceof List) {
            List<?> resultList = (List<?>) transactionResult;

            // resultList가 비어있지 않고 첫 번째 요소가 Long 타입일 때 처리
            if (!resultList.isEmpty() && resultList.get(0) instanceof Long) {
                Long result = (Long) resultList.get(0);

                // result가 1일 경우에만 쿠폰 발급 진행
                if (result == 1) {
                    CouponResponseDto couponResponseDto = decreaseCoupon(couponId);

                    // 유저 쿠폰에 추가
                    List<CouponEmbeddable> userCoupons = user.getCoupons();
                    userCoupons.add(CouponEmbeddable.of(couponResponseDto.getId(), couponResponseDto.getName()));
                    userRepository.save(user);

                    log.info("Coupon successfully issued for userId: {} with couponId: {}", userId, couponId);
                    return CouponResponseDto.of(couponId);
                } else {
                    log.warn("User {} has already received coupon {}", userId, couponId);
                }
            } else {
                log.warn("Unexpected transaction result format or empty result list: {}", transactionResult);
            }
        }

        throw new IllegalStateException("이미 쿠폰이 발급된 사용자입니다.");
    }

    public CouponResponseDto decreaseCoupon(Long couponId) {
        Coupon coupon = findCouponById(couponId);
        coupon.decreaseQuantity(1L);
        couponRepository.save(coupon);
        return CouponResponseDto.of(coupon.getId(), coupon.getName());
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CouponException.UserNotFoundException(userId));
    }

    private Coupon findCouponById(Long couponId) {
        return couponRepository.findByIdWithLock(couponId);
    }
}


/*
package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.api.coupon.exception.CouponException;
import com.example.goorm_ticket.domain.common.DistributedLock;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.entity.CouponEmbeddable;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final RedissonClient redissonClient;

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
*/
/*
    public CouponResponseDto allocateCouponToUser(Long userId, Long couponId) {
        RLock lock = redissonClient.getLock("coupon-lock-" + couponId);

        try {
            // 락을 획득 시도 (최대 5초 대기 후 10초 동안 락 유지)
            boolean available = lock.tryLock(500, 1000, TimeUnit.MILLISECONDS);
            if (!available) {
                throw new IllegalStateException("쿠폰 발급을 위한 락 획득에 실패했습니다.");
            }

            // 쿠폰 감소 및 사용자 쿠폰 추가 로직
            User user = findUserById(userId);
            CouponResponseDto couponResponseDto = decreaseCoupon(couponId);

            List<CouponEmbeddable> userCoupons = user.getCoupons();
            userCoupons.add(CouponEmbeddable.of(couponResponseDto.getId(), couponResponseDto.getName()));
            userRepository.save(user);

            return CouponResponseDto.of(couponId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Redis 락 획득 중 인터럽트가 발생했습니다.", e);
        } finally {
            // 락 해제
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }*//*


    @DistributedLock(key = "#couponId")
    public CouponResponseDto allocateCouponToUser(Long userId, Long couponId) {
        User user = findUserById(userId);

        CouponResponseDto couponResponseDto = decreaseCoupon(couponId);
        // 쿠폰 감소 로직이 성공하면 그 쿠폰을 유저에게 할당, 실패 시
        List<CouponEmbeddable> userCoupons = user.getCoupons();

        userCoupons.add(CouponEmbeddable.of(couponResponseDto.getId(), couponResponseDto.getName()));
        userRepository.save(user);

        return CouponResponseDto.of(couponId);
    }


    public CouponResponseDto decreaseCoupon(Long couponId) {
        Coupon coupon = findCouponById(couponId);
        coupon.decreaseQuantity(1L);
        couponRepository.save(coupon);
        return CouponResponseDto.of(coupon.getId(), coupon.getName());
    }

*/
/*    @Transactional
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
        // 쿠폰 감소 로직이 성공하면 그 쿠폰을 유저에게 할당, 실패 시
        List<CouponEmbeddable> userCoupons = user.getCoupons();

        userCoupons.add(CouponEmbeddable.of(couponResponseDto.getId(), couponResponseDto.getName()));
        userRepository.save(user);

        return CouponResponseDto.of(couponId);
    }*//*


    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CouponException.UserNotFoundException(userId));
    }


*/
/*    // 레디스 RLock을 이용할 때
    private Coupon findCouponById(Long couponId) {
        return couponRepository.findById(couponId).orElseThrow(() -> new CouponException.CouponNotFoundException(couponId));
    }*//*


    // 비관적 락을 이용할 때 or AOP를 이용할 때
    private Coupon findCouponById(Long couponId) {
        return couponRepository.findByIdWithLock(couponId);
    }
}*/
