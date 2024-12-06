package com.example.shared;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;

@EnableJpaAuditing
@EnableRetry
@EnableAspectJAutoProxy
@SpringBootApplication
public class SharedApplication {

    public static void main(String[] args) {
        SpringApplication.run(SharedApplication.class, args);
    }

}
