package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.api.coupon.exception.CouponException.CouponNotFoundException;
import com.example.goorm_ticket.api.coupon.exception.CouponException.CouponQuantityShortageException;
import com.example.goorm_ticket.api.coupon.service.strategy.AbstractCouponAllocation;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class RedisWaitingCouponService extends AbstractCouponAllocation {
    private final RedisTemplate<String, Long> redisTemplate;

    private static final String COUPON_COUNT_KEY = "coupon_count";
    private static final String COUPON_QUEUE_KEY = "coupon_queue";
    private static final int BATCH_SIZE = 10; // 한 번에 처리할 요청 수

    @Autowired
    public RedisWaitingCouponService(CouponRepository couponRepository,
                                     UserRepository userRepository,
                                     RedisTemplate<String, Long> redisTemplate) {
        super(couponRepository, userRepository);
        this.redisTemplate = redisTemplate;
    }

    // 사용자 요청을 큐에 추가
    private void addUserToQueue(Long userId) {
        boolean alreadyInQueue = redisTemplate.opsForZSet().rank(COUPON_QUEUE_KEY, userId) != null;

        if (!alreadyInQueue) {
            long timestamp = System.currentTimeMillis();
            log.info("대기열에 추가 - {} ({}초)", userId, timestamp);
            redisTemplate.opsForZSet().add(COUPON_QUEUE_KEY, userId, timestamp);
        } else {
            log.info("이미 대기열에 있는 사용자 ID: {}", userId);
        }
    }

    // 대기열에서 순서대로 쿠폰 발급
    @Transactional
    public void processCouponQueue(Long couponId) {
        Set<Long> users = redisTemplate.opsForZSet().range(COUPON_QUEUE_KEY, 0, BATCH_SIZE - 1);
        if (users == null || users.isEmpty()) {
            log.info("대기열에 더 이상 요청이 없습니다.");
            return;
        }

        Long remainingCoupons = redisTemplate.opsForValue().get(COUPON_COUNT_KEY);
        if (remainingCoupons == null || remainingCoupons <= 0) {
            log.info("쿠폰이 소진되었습니다.");
            return; // 쿠폰이 부족하면 발급을 중지
        }

        for (Long userId : users) {
            try {
                System.out.println("users = " + users);
                Long remainingCoupon = redisTemplate.opsForValue().get(COUPON_COUNT_KEY);
                redisTemplate.opsForZSet().remove(COUPON_QUEUE_KEY, userId);
                log.info("쿠폰 발급 성공 - 사용자 ID: {}", userId);
                CouponResponseDto couponResponseDto = decreaseCoupon(couponId);
            } catch (CouponQuantityShortageException e) {
                log.error("쿠폰 발급 실패: {}", e.getMessage());
                break; // 쿠폰이 부족할 경우 대기열 처리를 종료
            }
        }

        // 쿠폰 발급 후 남은 수량을 감소시킵니다.
        if (remainingCoupons > 0) {
            redisTemplate.opsForValue().decrement(COUPON_COUNT_KEY, Math.min(remainingCoupons, BATCH_SIZE));
        }

        // 대기열 상태 출력
        Set<Long> remainingUsers = redisTemplate.opsForZSet().range(COUPON_QUEUE_KEY, 0, -1);
        log.info("현재 대기열 상태: {}", remainingUsers);  // 대기열에 남아 있는 사용자 출력
        if (remainingUsers != null && !remainingUsers.isEmpty()) {
            log.info("남은 대기열 사용자 수: {}", remainingUsers.size());
        }
    }

    @Override
    public CouponResponseDto allocateCoupon(Long userId, Long couponId) {
        addUserToQueue(userId); // 사용자 요청을 대기열에 추가
        System.out.println("addUserToQueue, userId: " + userId);

        // 대기열을 처리하여 순차적으로 쿠폰 발급
        processCouponQueue(couponId);
        System.out.println("processCouponQueue, userId: " + userId);

        // 쿠폰 발급 결과 반환
        return CouponResponseDto.ofWithMessage(couponId, "쿠폰 요청이 처리되었습니다.");
    }
}