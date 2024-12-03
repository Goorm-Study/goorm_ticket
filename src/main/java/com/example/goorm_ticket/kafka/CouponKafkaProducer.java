package com.example.goorm_ticket.kafka;

import com.example.goorm_ticket.domain.coupon.dto.CouponEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class CouponKafkaProducer {
    private final KafkaTemplate<String, CouponEventDto> kafkaTemplate;

    public CompletableFuture<SendResult<String, CouponEventDto>> publishEvent(CouponEventDto couponEventDto) {
        return kafkaTemplate.send("couponTest", couponEventDto);
    }
}
