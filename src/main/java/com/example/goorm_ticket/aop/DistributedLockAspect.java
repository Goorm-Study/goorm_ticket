package com.example.goorm_ticket.aop;

import com.example.goorm_ticket.aop.annotation.DistributedLock;
import com.example.goorm_ticket.aop.util.CustomSpringELParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAspect {
    private static final String REDISSON_LOCK_PREFIX = "LOCK:";

    private final RedissonClient redissonClient;
    private final businessTransactionHandler businessTransactionHandler;

    @Around("@annotation(com.example.goorm_ticket.aop.annotation.DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        String key = REDISSON_LOCK_PREFIX + CustomSpringELParser.getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());
        RLock rLock = redissonClient.getLock(key);  // (1)

        try {
            boolean available = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());  // (2)
            if (!available) {
                return false;
            }

            return businessTransactionHandler.proceed(joinPoint);  // (3)
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            throw new InterruptedException(e.getMessage());
        } finally {
            try {
                rLock.unlock();   // (4)
            } catch (IllegalMonitorStateException e) {
                log.info("Redisson Lock Already UnLock {} {}",
                        kv("serviceName", method.getName()),
                        kv("key", key)
                );
            }
        }
    }

    public static String kv(String key, Object value) {
        return key + "=" + value;
    }

}