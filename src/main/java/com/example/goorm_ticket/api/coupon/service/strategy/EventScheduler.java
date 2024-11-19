package com.example.goorm_ticket.api.coupon.service.strategy;

import com.example.goorm_ticket.api.coupon.service.RedisWaitingCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventScheduler {

    @Autowired
    private final RedisWaitingCouponService redisWaitingCouponService;

    // 대기열을 주기적으로 처리하는 메서드
    @Scheduled(fixedRate = 1000) // 예: 1초마다 실행
    public void processCouponQueue(Long couponId) {

        log.info("대기열 쿠폰 발급 프로세스를 시작합니다.");

        // 대기열에서 순차적으로 쿠폰 발급을 처리
        redisWaitingCouponService.processCouponQueue(couponId);

        log.info("대기열 쿠폰 발급 프로세스가 완료되었습니다.");
    }

}
