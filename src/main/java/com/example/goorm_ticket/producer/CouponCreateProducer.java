package com.example.goorm_ticket.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class CouponCreateProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public CouponCreateProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void issue(Long userId, Long couponId) {
        String message = String.format("{\"userId\":%d, \"couponId\":%d}", userId, couponId);
        System.out.println("producer issue: " + message);

        try {
            kafkaTemplate.send("coupon_issue", message).get(); // 동기 호출로 전송 확인
            System.out.println("Message sent successfully!, userId:  " + userId);
        } catch (Exception e) {
            System.err.println("Failed to send message: " + e.getMessage());
        }
    }
}
