package com.example.goorm_ticket.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponKafkaProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishEvent(String message) {
        kafkaTemplate.send("coupon", message);
    }
}
