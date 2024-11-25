package com.example.consumer.consumer;


import com.example.consumer.domain.Coupon;
import com.example.consumer.domain.User;
import com.example.consumer.repository.CouponRepository;
import com.example.consumer.repository.UserRepository;
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

    private final UserRepository userRepository;
    private final CouponRepository couponRepository;

    private final Logger logger = LoggerFactory.getLogger(CouponCreatedConsumer.class);

    public CouponCreatedConsumer(UserRepository userRepository, CouponRepository couponRepository) {
        this.userRepository = userRepository;
        this.couponRepository = couponRepository;
    }

    @RetryableTopic(
            attempts = "3", // 최대 3번 재시도
            backoff = @Backoff(delay = 2000), // 재시도 간격 2초
            dltTopicSuffix = "-dlt", // 실패 시 이동할 Dead Letter Topic 접미사
            autoCreateTopics = "true" // DLQ 토픽 자동 생성
    )
    @KafkaListener(topics = "coupon_issue", groupId = "coupon-group")
    @Transactional
    public void listener(String message, Acknowledgment acknowledgment) {
        try {
            logger.info("쿠폰 발급 요청 시작 - message={}", message);
            JSONObject json = new JSONObject(message);
            System.out.println("json = " + json);
            Long userId = json.getLong("userId");
            Long couponId = json.getLong("couponId");

            // 특정 userId에 대해 예외 발생
            if (userId == 1) { // 예: userId가 12345일 경우
                logger.error("발급 제한: userId={}", userId);
                throw new RuntimeException("발급 제한된 사용자");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            Coupon coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new RuntimeException("쿠폰을 찾을 수 없습니다."));

            //쿠폰 발급
            user.addCoupon(coupon);
            userRepository.save(user);

            logger.info("쿠폰 발급 성공 - userId={}, couponId={}", userId, couponId);

            // 메시지 처리 성공 후 오프셋 커밋
            acknowledgment.acknowledge();
        } catch (Exception e) {
            logger.error("Message processing failed: {}", message, e);
            // 처리 실패 시 커밋하지 않음 (메시지가 재처리됨)
        }
    }

}