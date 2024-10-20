package com.example.goorm_ticket.api.coupon.service;

import com.example.goorm_ticket.api.coupon.exception.CouponException;
import com.example.goorm_ticket.domain.coupon.entity.Coupon;
import com.example.goorm_ticket.domain.user.entity.User;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RedisCouponService {

    private static final Integer BATCHSIZE = 5;
    private static final String COUPON_KEY = "coupon-queue";
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    // 대기열에 사용자 5명을 추가하는 메서드
    public void addToQueue(Long userId, Long couponId) {
        User user = findUserById(userId);
        redisTemplate.opsForList().rightPush(COUPON_KEY, String.valueOf(userId + ":" + couponId)); // leftPush는 'LIFO', rightPush는 'FIFO'이다.
    }

    // 5명씩 Batchsize를 구성하여 대기열에 등록
    public List<String> getBatchUsers(){
        List<String> users = redisTemplate.opsForList().range(COUPON_KEY, 0, BATCHSIZE - 1);

        if(!users.isEmpty() && users != null){
            redisTemplate.opsForList().trim(COUPON_KEY, 0, BATCHSIZE - 1); // 가져온 대기열의 요청 수만큼 큐에서 제거.
        }
        return users;
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CouponException.UserNotFoundException(userId));
    }
}
