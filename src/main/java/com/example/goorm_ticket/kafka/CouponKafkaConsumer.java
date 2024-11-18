package com.example.goorm_ticket.kafka;

import com.example.goorm_ticket.api.coupon.exception.CouponException;
import com.example.goorm_ticket.api.coupon.exception.CouponException.CouponEventDeserializationException;
import com.example.goorm_ticket.api.coupon.service.CouponService;
import com.example.goorm_ticket.domain.coupon.dto.CouponRequestDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CouponKafkaConsumer {
    private final ObjectMapper objectMapper;
    private final CouponService couponService;
    @KafkaListener(topics = "coupon", groupId = "consumer-group01")
    public void consumeEvent(String message) {
        log.info("consumed message: {}", message);
        CouponRequestDto couponRequestDto;
        try {
            couponRequestDto = objectMapper.readValue(message, CouponRequestDto.class);
        } catch (JsonProcessingException e) {
            throw new CouponEventDeserializationException();
        }
        couponService.allocateCouponToUserWithDistributedLock(couponRequestDto.getUserId(), couponRequestDto.getCouponId());
    }
}
