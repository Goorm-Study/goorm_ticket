package com.example.goorm_ticket.kafka;

import com.example.goorm_ticket.domain.coupon.dto.CouponEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponKafkaProducer {
    private final KafkaTemplate<String, CouponEventDto> kafkaTemplate;

    public void publishEvent(CouponEventDto couponEventDto) {
        kafkaTemplate.send("couponTest", couponEventDto);
    }
}
