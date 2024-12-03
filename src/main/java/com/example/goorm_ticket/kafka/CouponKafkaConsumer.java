package com.example.goorm_ticket.kafka;

import com.example.goorm_ticket.api.coupon.exception.CouponException;
import com.example.goorm_ticket.api.coupon.service.CouponService;
import com.example.goorm_ticket.domain.coupon.dto.CouponEventDto;
import com.example.goorm_ticket.domain.coupon.dto.CouponResponseDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.entity.CouponCompensateEvent;
import com.example.goorm_ticket.domain.coupon.repository.CouponCompensateEventRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class CouponKafkaConsumer {

    private final CouponService couponService;
    private final ApplicationEventPublisher eventPublisher;
    private final CouponCompensateEventRepository couponCompensateEventRepository;
    @KafkaListener(topics = "couponTest", groupId = "coupongroup")
    @Transactional
    public void consumeEvent(List<CouponEventDto> events, Acknowledgment ack) {
        // 취소된 이벤트(CouponException)들에 대해선 따로 레디스 보상(해당 쿠폰 수량 롤백)
        CancelledCouponEvent cancelledEvents = new CancelledCouponEvent();

        log.info("배치 사이즈: {}", events.size());
        for(CouponEventDto event: events) {
            if(event == null) {
                log.error("메시지 역직렬화 실패, JSON 형식 확인 필요");
            }
            Long couponId = event.getCouponId();
            try {
                Long userId = event.getUserId();
                allocateCouponToUser(userId, couponId);
            } catch (CouponException e) {
                log.warn("쿠폰 발급 불가, {}", e.getMessage());
                cancelledEvents.add(couponId);
            }
        }

        if(!cancelledEvents.getCouponQuantity().isEmpty()) {
            eventPublisher.publishEvent(cancelledEvents);
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
    public void insertCouponCompensateEvent(CancelledCouponEvent cancelledCouponEvent) {
        cancelledCouponEvent.getCouponQuantity().forEach((couponId, quantity) -> {
            CouponCompensateEvent couponCompensateEvent = CouponCompensateEvent.of(couponId, quantity);
            couponCompensateEventRepository.save(couponCompensateEvent);
        });
    }
}
