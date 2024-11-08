package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.api.coupon.exception.CouponException;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCouponServiceV2 {

    private static final Integer BATCH_SIZE = 5;
    private static final String WAIT_KEY = "wait-queue";
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final CouponService couponService;

    public Long addToQueue(Long userId, Long couponId) {
        if(userId == null || couponId == null) {
            throw new IllegalArgumentException("userId and couponId must not be null");
        }

        if(!existsUserById(userId)) {
            log.error("사용자(ID: {}) 정보가 존재하지 않습니다.", userId);
            throw new CouponException.UserNotFoundException(userId);
        }

        String value = userId + ":" + couponId;
        redisTemplate.opsForZSet().add(WAIT_KEY, value, System.currentTimeMillis());
        Long rank = redisTemplate.opsForZSet().rank(WAIT_KEY, value);
        if(rank == null) {
            log.error("순번을 조회할 수 없습니다.");
            throw new CouponException.RankNotFoundException(userId, couponId);
        }
        return rank + 1;
    }

    public void publish(){
        Set<String> works = redisTemplate.opsForZSet().range(WAIT_KEY, 0, BATCH_SIZE - 1);
        List<String> toRemove = new ArrayList<>();

        if(works != null && !works.isEmpty()) {
            for(String work : works){
                String[] p = work.split(":");
                if(p.length != 2) {
                    log.error("Invalid work format: {}", work);
                    toRemove.add(work);
                    continue;
                }
                try {
                    Long userId = Long.parseLong(p[0]);
                    Long couponId = Long.parseLong(p[1]);
                    couponService.allocateCouponToUser(userId, couponId);
                    toRemove.add(work);
                } catch (NumberFormatException e) {
                    log.error("Invalid number format in work: {}", work, e);
                    toRemove.add(work);
                }
            }
        }

        if(!toRemove.isEmpty()) {
            redisTemplate.opsForZSet().remove(WAIT_KEY, toRemove);
        }
    }

    private boolean existsUserById(Long userId) {
        return userRepository.existsById(userId);
    }
}