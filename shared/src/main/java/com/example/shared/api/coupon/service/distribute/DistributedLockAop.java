package com.example.shared.api.coupon.service.distribute;

import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAop {
    private static final String REDISSON_LOCK_PREFIX = "LOCK:";

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;

    @Around("@annotation(com.example.shared.api.coupon.service.distribute.DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        String key = REDISSON_LOCK_PREFIX + CustomSpringELParser.getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());
        RLock rLock = redissonClient.getLock(key);  // (1)

        try {
            boolean available = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());  // (2)
            if (!available) {
                log.info("락 획득 실패. !available");
                return false;
            }
            log.info("락 획득 성공, key: " + key);
            // AOP가 실제 비즈니스 로직을 실행하는 부분
            return aopForTransaction.proceed(joinPoint);  // (3)
        } catch (InterruptedException e) {
            log.info("락 인터럽트 발생");
            throw new InterruptedException();
        } finally {
            try {
                rLock.unlock();   // (4)
                log.info("락 해제 성공");
            } catch (IllegalMonitorStateException e) {
                log.info("Redisson Lock Already UnLock");
            }
        }
    }
}