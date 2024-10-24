package com.example.goorm_ticket.aop;

import com.example.goorm_ticket.aop.annotation.NamedLock;
import com.example.goorm_ticket.aop.util.CustomSpringELParser;
import com.example.goorm_ticket.api.coupon.exception.CouponException;
import com.example.goorm_ticket.lockdomain.LockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class NamedLockAspect {
    private final LockRepository lockRepository;
    private final businessTransactionHandler businessTransactionHandler;
    private final TransactionTemplate transactionTemplate;
    private static final String NAMED_LOCK_PREFIX = "LOCK:";

    @Around("@annotation(com.example.goorm_ticket.aop.annotation.NamedLock)")
    public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        NamedLock namedLock = method.getAnnotation(NamedLock.class);

        String lockKey = NAMED_LOCK_PREFIX + CustomSpringELParser.getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), namedLock.lockKey());
        return transactionTemplate.execute(status -> {
            try {
                lockRepository.getLock(lockKey, namedLock.waitTime());
                return businessTransactionHandler.proceed(joinPoint);
            } catch (CouponException e) {
                throw e;
            } catch (Throwable e) {
                log.error("쿠폰 발급(네임드락) 도중 예외 발생: {}", e.getMessage());
                throw new RuntimeException(e);
            } finally {
                lockRepository.releaseLock(lockKey);
            }
        });
    }
}
