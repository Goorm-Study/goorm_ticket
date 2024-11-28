package com.example.goorm_ticket.config;

import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class CouponDataInitializer {

    private static final String REDIS_COUPON_PREFIX = "COUPON:";
    private final StringRedisTemplate stringRedisTemplate;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    public CouponDataInitializer(StringRedisTemplate stringRedisTemplate, CouponRepository couponRepository, UserRepository userRepository) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.couponRepository = couponRepository;
        this.userRepository = userRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeCouponData() {
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
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