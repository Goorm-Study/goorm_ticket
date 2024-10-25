package com.example.goorm_ticket.global.component;

import com.example.goorm_ticket.api.coupon.service.RedisCouponService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CouponScheduler {

    @Autowired
    private JobLauncher jobLauncher;


    private Job job;
    @Autowired
    private RedisCouponService redisCouponService;

    @Scheduled(fixedRate = 1000) // 1초마다 배치가 작동하도록 설정
    public void runCouponBatch() throws Exception{
        //쿠폰이 남아있지 않다면 종료시키는 메서드 추가
        redisCouponService.publish();
        redisCouponService.getBatchUsers();
    }
}
