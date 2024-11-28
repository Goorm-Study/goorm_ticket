package com.example.goorm_ticket.global.component;

import com.example.goorm_ticket.api.coupon.service.CouponService;
import com.example.goorm_ticket.config.RedisCouponConstants;
import com.example.goorm_ticket.domain.coupon.dto.CouponEventDto;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.CouponCompensateEvent;
import com.example.goorm_ticket.domain.coupon.repository.CouponCompensateEventRepository;
import com.example.goorm_ticket.domain.coupon.repository.CouponProcessedEventRepository;
import com.example.goorm_ticket.kafka.CouponKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponScheduler {

    private final CouponCompensateEventRepository couponCompensateEventRepository;
    private final CouponProcessedEventRepository couponProcessedEventRepository;
    private final CouponKafkaProducer couponKafkaProducer;
    private final CouponService couponService;
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
        log.info("레디스 쿠폰 수량 보상 완료");
    }

    // 이벤트 재발행 하기 전 DB에 해당 이벤트 id가 저장되어있는지 확인, 저장되어 있다면 실제로는 처리된거니까 재발행하지 않고 그냥 없애기
    // 근데 이렇게하면 컨슈머에서 처리하기 전에 이 스케쥴러 실행되면 앞으로 처리할 이벤트를 또 발행할텐데...
    // 타임스탬프를 넣어서 일정 시간 미만의 pending 이벤트는 아직 컨슈머가 처리하지 못한걸수도 있으니까 넘어가기..?
    @Scheduled(fixedDelay = 10000)
    public void republishCouponEvent() {
        // O(N)이어서 좀..
        List<Long> pendingEvents = stringRedisTemplate.opsForSet().members("PENDING_EVENT").stream().map(Long::valueOf).toList();
        pendingEvents.forEach(eventId -> {
            if(couponProcessedEventRepository.existsByEventId(eventId)) {
                couponService.removePendingEvent(eventId);
            } else { // 이벤트 재발행
                republishEvent(eventId);
            }
        });
    }

    private void republishEvent(Long eventId) {
        List<Object> values = stringRedisTemplate.opsForHash().multiGet("PENDING:" + eventId, List.of("userId", "couponId"));
        Long userId = Long.valueOf((String)values.get(0));
        Long couponId = Long.valueOf((String)values.get(1));
        couponKafkaProducer.publishEvent(CouponEventDto.of(userId, couponId, eventId));
    }

}
