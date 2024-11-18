package com.example.goorm_ticket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class CouponRedisScriptConfig {

    @Bean
    public RedisScript<Long> decrAndSaveMessageScript() {
        ClassPathResource script = new ClassPathResource("scripts/decrAndSaveMessage.lua");
        return RedisScript.of(script, Long.class);
    }
}
