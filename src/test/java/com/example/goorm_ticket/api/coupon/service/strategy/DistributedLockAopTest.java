package com.example.goorm_ticket.api.coupon.service.strategy;

import static org.junit.jupiter.api.Assertions.*;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.junit.jupiter.api.Test;

class DistributedLockAopTest {

    @Test
    @Around("execution(* com.example..*(..))")
    public Object simpleAopTest(final ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("AOP 테스트: 메서드 실행");
        return joinPoint.proceed();
    }

}