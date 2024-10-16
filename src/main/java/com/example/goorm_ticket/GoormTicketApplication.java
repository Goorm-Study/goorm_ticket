package com.example.goorm_ticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;

@EnableJpaAuditing
@EnableRetry
@SpringBootApplication
public class GoormTicketApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoormTicketApplication.class, args);
    }

}
