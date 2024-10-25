package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.api.coupon.exception.CouponException;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCouponService {

    private static final Integer BATCHSIZE = 5;
    private static final String WAIT_KEY = "wait-queue";
    private static final String ENTER_KEY = "enter-queue";
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final CouponService couponService;

    // WAIT 대기열에 사용자를 추가하는 메서드
    public void addToQueue(Long userId, Long couponId) {
        User user = findUserById(userId);
        String value = userId + ":" + couponId;

        Long rank = redisTemplate.opsForZSet().rank(WAIT_KEY, value); // value에 대한 순위 측정
        if(rank == null) {
            redisTemplate.opsForZSet().add(WAIT_KEY, value, System.currentTimeMillis()); // 존재하지 않으므로 추가
            log.info("해당 {}는 대기열에 존재하지 않습니다.", value);
        }
        else{
            redisTemplate.opsForZSet().add(WAIT_KEY, value, System.currentTimeMillis()); // 대기열 이미 존재 시, 맨 뒤로 다시 보낸다.
            log.info("해당 {}는 대기열에 이미 존재합니다.", value);
        }
    }

    // 현재 대기열의 순번을 사용자에게 제공
    public void getBatchUsers(){

        Set<String> users = redisTemplate.opsForZSet().range(ENTER_KEY, 0, -1);

        if(!users.isEmpty() && users != null) {
            for (String user : users) {
                Long rank = redisTemplate.opsForZSet().rank(WAIT_KEY, user);
                log.info("현재 고객님의 순번은 {}번째입니다.", rank);
            }
        }
    }

    // Enter 대기열에 들어온 이들에게 쿠폰을 발급
    public void publish(){
        Set<String> enterQueue = redisTemplate.opsForZSet().range(ENTER_KEY, 0, BATCHSIZE-1);
        if(!enterQueue.isEmpty() && enterQueue != null) {
            for(Object person : enterQueue){
                String[] p = person.toString().split(":");
                Long userId = Long.parseLong(p[0]);
                Long couponId = Long.parseLong(p[1]);
                couponService.allocateCouponToUser(userId, couponId);
                redisTemplate.opsForZSet().remove(ENTER_KEY, person);
                log.info("{}님에게 쿠폰이 발급되었습니다!", userId);
            }
        }
    }

    private User findUserById(Long userId) {
        log.info("hi: {}", userId);
        return userRepository.findById(userId).orElseThrow(() -> new CouponException.UserNotFoundException(userId));
    }
}
