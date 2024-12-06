package org.example.consumer.consumer;

import org.example.consumer.repository.FailedEventRepository;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class DLQConsumer {
    private final FailedEventRepository failedEventRepository;
    private final Logger logger = LoggerFactory.getLogger(DLQConsumer.class);

    public DLQConsumer(FailedEventRepository failedEventRepository) {
        this.failedEventRepository = failedEventRepository;
    }

    @KafkaListener(topics = "coupon_issue-dlt", groupId = "dlt-group")
    public void listener(String message, Acknowledgment acknowledgment) {
        try {
            logger.error("쿠폰 발급 실패한 요청 DLQ - {}", message);

            // 실패 메시지를 DB에 저장
            JSONObject json = new JSONObject(message);
            Long userId = json.getLong("userId");

            FailedEvent failedEvent = new FailedEvent(userId, message);
            failedEventRepository.save(failedEvent);

            logger.info("실패한 이벤트 데이터 저장 - userId={}, message={}", userId, message);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            logger.error("DLQ 저장 Error: {}", message, e);
            //DLQ 처리 완
        }
    }
}
