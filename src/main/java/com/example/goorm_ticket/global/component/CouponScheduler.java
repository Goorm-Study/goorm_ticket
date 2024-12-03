package com.example.goorm_ticket.global.component;

import com.example.goorm_ticket.api.coupon.service.CouponService;
import com.example.goorm_ticket.config.RedisCouponConstants;
import com.example.goorm_ticket.domain.coupon.entity.CouponCompensateEvent;
import com.example.goorm_ticket.domain.coupon.repository.CouponCompensateEventRepository;
import com.example.goorm_ticket.kafka.CouponKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponScheduler {

    private final CouponCompensateEventRepository couponCompensateEventRepository;
    private final StringRedisTemplate stringRedisTemplate;
    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void compensateCouponQuantity() {
        List<CouponCompensateEvent> events = couponCompensateEventRepository.findAll();
        events.forEach(event -> {
            String couponKey = RedisCouponConstants.getRedisCouponKey(event.getCouponId());
            stringRedisTemplate.opsForHash().increment(couponKey, "quantity", event.getQuantity());
        });
        couponCompensateEventRepository.deleteAllInBatch();
    }
}
