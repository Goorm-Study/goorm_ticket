package com.example.goorm_ticket.global.redis;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "spring.data.redis")
@Configuration
public class RedisProperty {

    private String host;
    private Integer port;
    private RedisProperty master;
    private List<RedisProperty> slaves;
}
