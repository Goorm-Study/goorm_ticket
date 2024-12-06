package org.example.consumer.consumer;

import org.example.consumer.repository.FailedEventRepository;
import org.example.consumer.service.KafkaCouponService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CouponCreatedConsumer {

    private final KafkaCouponService allocationService;
    private final FailedEventRepository failedEventRepository;

    private final Logger logger = LoggerFactory.getLogger(CouponCreatedConsumer.class);

    public CouponCreatedConsumer(KafkaCouponService allocationService, FailedEventRepository failedEventRepository) {
        this.allocationService = allocationService;
        this.failedEventRepository = failedEventRepository;
    }

    @RetryableTopic(
            attempts = "3", // 최대 3번 재시도
            backoff = @Backoff(delay = 2000), // 재시도 간격 2초
            dltTopicSuffix = "-dlt", // 실패 시 이동할 Dead Letter Topic 접미사
            autoCreateTopics = "true" // DLQ 토픽 자동 생성
    )
    @KafkaListener(topics = "coupon_issue", groupId = "coupon-group")
    public void listener(String message, Acknowledgment acknowledgment) {
        Long userId = null;

        try {
            logger.info("쿠폰 발급 요청 시작 - message={}", message);
            JSONObject json = new JSONObject(message);
            System.out.println("json = " + json);
            userId = json.getLong("userId");
            Long couponId = json.getLong("couponId");

            // 특정 userId에 대해 예외 발생
            if (userId == 1) { // 예: userId가 12345일 경우
                logger.error("발급 제한: userId={}", userId);
                throw new RuntimeException("발급 제한된 사용자");
            }

            allocationService.allocate(userId, couponId);

            // 메시지 처리 성공 후 오프셋 커밋
            acknowledgment.acknowledge();
        } catch (Exception e) {
            logger.error("Message processing failed: {}", message, e);
            failedEventRepository.save(new FailedEvent(userId, e.getMessage()));

            throw new RuntimeException(e.getMessage());
            // 처리 실패 시 커밋하지 않음
        }
    }

}