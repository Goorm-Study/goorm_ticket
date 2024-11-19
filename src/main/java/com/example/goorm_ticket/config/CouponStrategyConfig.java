package com.example.goorm_ticket.config;

import com.example.goorm_ticket.api.coupon.service.RedisWaitingCouponService;
import com.example.goorm_ticket.api.coupon.service.strategy.*;
import com.example.goorm_ticket.domain.coupon.repository.CouponRepository;
import com.example.goorm_ticket.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@EnableAspectJAutoProxy
public class CouponStrategyConfig {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Long> redisTemplate;

    public CouponStrategyConfig(CouponRepository couponRepository, UserRepository userRepository, RedisTemplate<String, Long> redisTemplate) {
        this.couponRepository = couponRepository;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }

    @Bean
    public CouponAllocationStrategy noLockStrategy() {
        return new NoLockStrategy(couponRepository, userRepository);
    }

    @Bean
    public CouponAllocationStrategy optimisticLockStrategy() {
        return new OptimisticLockStrategy(couponRepository, userRepository);
    }

    @Bean
    @Primary
    public CouponAllocationStrategy pessimisticLockStrategy() {
        return new PessimisticLockStrategy(couponRepository, userRepository);
    }

    @Bean
    public CouponAllocationStrategy redisWatingCouponService() {
        return new RedisWaitingCouponService(couponRepository, userRepository, redisTemplate);
    }

    @Bean
    public CouponAllocationStrategy couponAllocationStrategy() {
        // 여기서 사용할 기본 전략을 선택
        //return optimisticLockStrategy();

        return pessimisticLockStrategy();
        // return noLockStrategy();
        //return redisWatingCouponService();
    }
}