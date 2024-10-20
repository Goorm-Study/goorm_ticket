package com.example.goorm_ticket.global.component;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BatchScheduler {

    @Autowired
    private JobLauncher jobLauncher;


    private Job job;

    @Scheduled(fixedRate = 6000) // 6초마다 배치가 작동하도록 설정
    public void runCouponBatch() throws Exception{
        jobLauncher.run(job, new JobParametersBuilder().toJobParameters());
    }
}
