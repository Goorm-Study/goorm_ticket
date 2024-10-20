package com.example.goorm_ticket.api.coupon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@StepScope
@RequiredArgsConstructor
public class CouponBatchTasklet implements Tasklet {

    private final RedisCouponService redisCouponService;
    private final CouponService couponService;


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        List<String> users = redisCouponService.getBatchUsers(); // 5만큼 설정한 요청 수를 가져옴.

        if (!users.isEmpty() && users != null){
            for( String user : users){
                String[] parts = user.split(":");  // "userId:couponId" 분리
                Long userId = Long.valueOf(parts[0]);
                Long couponId = Long.valueOf(parts[1]);
                couponService.allocateCouponToUser(userId, couponId);
            }
        }
    }
}
