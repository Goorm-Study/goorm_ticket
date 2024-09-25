package com.example.goorm_ticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class GoormTicketApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoormTicketApplication.class, args);
    }

}
