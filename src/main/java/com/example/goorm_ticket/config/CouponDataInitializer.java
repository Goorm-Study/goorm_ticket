package com.example.goorm_ticket.config;

import com.example.goorm_ticket.domain.coupon.dto.CouponEventDto;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import com.example.goorm_ticket.kafka.CouponKafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CouponDataInitializer {

    private static final String REDIS_COUPON_PREFIX = "COUPON:";
    private final StringRedisTemplate stringRedisTemplate;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final CouponKafkaProducer couponKafkaProducer;

    @EventListener(ApplicationReadyEvent.class)
    private void initializeCouponData() {
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            User user = createUser("tester" + i);
            users.add(user);
        }
        userRepository.saveAll(users);
        Coupon coupon1 = Coupon.of(100L, "coupon1", 20.0, LocalDateTime.of(2024, 12, 31, 0, 0));
        Coupon coupon2 = Coupon.of(100L, "coupon2", 20.0, LocalDateTime.of(2024, 12, 31, 0, 0));
        Coupon coupon3 = Coupon.of(100L, "coupon3", 20.0, LocalDateTime.of(2024, 12, 31, 0, 0));
        couponRepository.saveAll(List.of(coupon1, coupon2, coupon3));

        stringRedisTemplate.getConnectionFactory().getConnection().flushDb();
        cacheCouponToRedis(coupon1);
        cacheCouponToRedis(coupon2);
        cacheCouponToRedis(coupon3);
//        List<Coupon> couponList = couponRepository.findAll();
//        couponList.forEach(this::cacheCouponToRedis);
    }

    @EventListener(ApplicationReadyEvent.class)
    private void republishUnsentCouponEvent() {
        List<Long> pendingEvents = stringRedisTemplate.opsForSet().members("PENDING_EVENT").stream().map(Long::valueOf).toList();
        pendingEvents.forEach(this::republishEvent);
    }

    private void republishEvent(Long eventId) {
        List<Object> values = stringRedisTemplate.opsForHash().multiGet("PENDING:" + eventId, List.of("userId", "couponId"));
        Long userId = Long.valueOf((String)values.get(0));
        Long couponId = Long.valueOf((String)values.get(1));
        couponKafkaProducer.publishEvent(CouponEventDto.of(userId, couponId, eventId));
    }

    private static User createUser(String username) {
        return User.builder()
                .username(username)
                .password("1234")
                .build();
    }

    private static String getRedisCouponKey(Long couponId) {
        return REDIS_COUPON_PREFIX + couponId;
    }

    private void cacheCouponToRedis(Coupon coupon) {
        String key = getRedisCouponKey(coupon.getId());
        stringRedisTemplate.opsForHash().put(key, "couponId", coupon.getId().toString());
        stringRedisTemplate.opsForHash().put(key, "quantity", coupon.getQuantity().toString());
    }
}