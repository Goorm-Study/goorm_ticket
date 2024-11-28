package com.example.goorm_ticket.kafka;

import com.example.goorm_ticket.api.coupon.exception.CouponException;
import com.example.goorm_ticket.api.coupon.service.CouponService;
import com.example.goorm_ticket.config.RedisCouponConstants;
import com.example.goorm_ticket.domain.coupon.dto.CouponEventDto;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.entity.CouponCompensateEvent;
import com.example.goorm_ticket.domain.coupon.entity.CouponProcessedEvent;
import com.example.goorm_ticket.domain.coupon.repository.CouponCompensateEventRepository;
import com.example.goorm_ticket.domain.coupon.repository.CouponProcessedEventRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class CouponKafkaConsumer {

    private final CouponService couponService;
    private final ApplicationEventPublisher eventPublisher;
    private final CouponCompensateEventRepository couponCompensateEventRepository;
    private final CouponProcessedEventRepository couponProcessedEventRepository;
    private final StringRedisTemplate stringRedisTemplate;
    @KafkaListener(topics = "couponTest", groupId = "coupongroup")
    @Transactional
    public void consumeEvent(List<CouponEventDto> events, Acknowledgment ack) {
        // 그냥 user_coupons에 insert해서 중복검사 하지 말고 유니크한지 select 쿼리로 중복 검사
        // 유저 존재하고, 쿠폰 존재하고, 중복 아닌지 검사해서 다 통과한 애들만 트랜잭션으로 쿠폰 수량 감소하고 쿠폰 발급하기
        // 일단 성공/실패 여부와 관계없이 처리한 이벤트들에 대해선 PENDING SET에서 제거, 실패한 이벤트들에 대해선 따로 레디스 보상(해당 쿠폰 수량 롤백)
        ProcessedCouponEvent processedEvents = new ProcessedCouponEvent();
        FailedCouponEvent failedEvents = new FailedCouponEvent();

        log.info("배치 사이즈: {}", events.size());
        for(CouponEventDto event: events) {
            if(event == null) {
                log.error("메시지 역직렬화 실패, JSON 형식 확인 필요");
            }
            Long couponId = event.getCouponId();
            Long eventId = event.getEventId();
            processedEvents.add(eventId);
            // 처리된 이벤트 id를 DB에 저장
            couponProcessedEventRepository.save(CouponProcessedEvent.of(eventId));
            try {
                Long userId = event.getUserId();
                allocateCouponToUser(userId, couponId);
            } catch (CouponException e) {
                log.warn("쿠폰 발급 불가, {}", e.getMessage());
                failedEvents.add(couponId);
            }
        }

        // AckMode.BATCH로 설정하면 리스너 invoke 후 오프셋 커밋되므로 트랜잭션은 커밋됐는데 오프셋 커밋은 실패할 시 DB에 반영된 이벤트들을 중복으로 처리할 가능성이 존재
        // AckMode.MANUAL로 설정하면 리스너 내부에서 명시적으로 오프셋 커밋을 시도하기 때문에 오프셋 커밋 실패 시 트랜잭션도 롤백될 것으로 예상
        // 당장은 트랜잭셔널 아웃박스 패턴으로 레디스 명령 보낼거까지 db에 저장하고 이후에 db에서 꺼내오는거밖에 해결책이 안떠오름...
        if(!processedEvents.getEventIdList().isEmpty()) {
            eventPublisher.publishEvent(processedEvents);
        }
        if(!failedEvents.getCouponQuantity().isEmpty()) {
            eventPublisher.publishEvent(failedEvents);
        }
        ack.acknowledge();
    }

    private void allocateCouponToUser(Long userId, Long couponId) {
        User user = couponService.findUserById(userId);
        Coupon coupon = couponService.findCouponById(couponId);
        couponService.addCouponToUserCoupons(user, CouponResponseDto.of(couponId, coupon.getName()));
        couponService.decreaseCoupon(couponId);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void insertCouponCompensateEvent(FailedCouponEvent failedCouponEvent) {
        failedCouponEvent.getCouponQuantity().forEach((couponId, quantity) -> {
            CouponCompensateEvent couponCompensateEvent = CouponCompensateEvent.of(couponId, quantity);
            couponCompensateEventRepository.save(couponCompensateEvent);
        });
        log.info("아웃박스에 레디스 쿠폰 수량 보상 이벤트 저장 완료");
    }

    /* 만약 이 메소드가 실패하면 PENDING set에서 제거하지 않으므로 메시지 처리가 안된것으로 판단하고 다시 처리되도록 전송할텐데 그럼 결과적으로 중복 보상이 될텐데
     PENDING SET에 이벤트를 추가할 때 고유 이벤트 id를 넣기, 메시지를 처리할 때 트랜잭션 내에서 DB에 해당 이벤트 id를 어디에 저장해놓기
     주기적으로 PENDING SET에 있는 이벤트 재발행 하기 전 DB에 해당 이벤트 id가 저장되어있는지 확인, 저장되어 있다면 실제로는 처리된거니까 재발행하지 않고 그냥 없애기
    */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    // @Async
    public void deleteRedisPendingEvent(ProcessedCouponEvent processedEvents) {
        processedEvents.getEventIdList().forEach(eventId -> couponService.removePendingEvent(eventId));
        log.info("처리 성공한 이벤트 레디스 PENDING_EVENT SET에서 제거");
    }

    // 스케쥴러가 해야 하는거: 전송안된 PENDING 이벤트 재전송, 아웃박스에 저장된 이벤트 확인해서 레디스에 수량 롤백

}
