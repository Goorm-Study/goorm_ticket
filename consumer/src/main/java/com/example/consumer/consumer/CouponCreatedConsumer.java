package com.example.consumer.consumer;


import com.example.consumer.domain.Coupon;
import com.example.consumer.domain.CouponEmbeddable;
import com.example.consumer.domain.User;
import com.example.consumer.repository.CouponRepository;
import com.example.consumer.repository.FailedEventRepository;
import com.example.consumer.repository.UserRepository;
import java.util.List;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CouponCreatedConsumer {

    private final UserRepository userRepository;
    private final FailedEventRepository failedEventRepository;
    private final CouponRepository couponRepository;

    private final Logger logger = LoggerFactory.getLogger(CouponCreatedConsumer.class);

    public CouponCreatedConsumer(UserRepository userRepository, FailedEventRepository failedEventRepository,
                                 CouponRepository couponRepository) {
        this.userRepository = userRepository;
        this.failedEventRepository = failedEventRepository;
        this.couponRepository = couponRepository;
    }

    @KafkaListener(topics = "coupon_issue", groupId = "coupon-group")
    @Transactional
    public void listener(String message) {
        JSONObject json = new JSONObject(message);
        System.out.println("json = " + json);
        Long userId = json.getLong("userId");
        Long couponId = json.getLong("couponId");

        try {
            logger.info("user조회 전");
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            logger.info("user조회 후");

            Coupon coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new RuntimeException("쿠폰을 찾을 수 없습니다."));
            logger.info("쿠폰 조회 후");

            logger.info("쿠폰 발급 전");
            //쿠폰 발급
            user.addCoupon(coupon);
            logger.info("쿠폰 발급 후");

            userRepository.save(user);

        } catch (Exception e) {
            logger.error("failed to issue coupon: " + e.getMessage() + " userId: " + userId);
            failedEventRepository.save(new FailedEvent(userId));
        }
    }
}