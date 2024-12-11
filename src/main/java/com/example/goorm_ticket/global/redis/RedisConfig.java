package com.example.goorm_ticket.global.redis;

import io.lettuce.core.ReadFrom;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
@EnableRedisRepositories(basePackageClasses = RedisConfig.class)
public class RedisConfig {

    private final RedisProperty redisProperty;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
//        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
//                .readFrom(ReadFrom.REPLICA_PREFERRED)
//                .build();
//
//        RedisStaticMasterReplicaConfiguration slaveConfig = new RedisStaticMasterReplicaConfiguration(
//                redisProperty.getMaster().getHost(), redisProperty.getMaster().getPort()
//        );
//
//        redisProperty.getSlaves().forEach(slave -> slaveConfig.addNode(slave.getHost(), slave.getPort()));
//
//        return new LettuceConnectionFactory(slaveConfig, clientConfig);

        RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration()
                .master("redis-master")
                .sentinel("localhost", 26379)
                .sentinel("localhost", 26380)
                .sentinel("localhost", 26381);

        return new LettuceConnectionFactory(redisSentinelConfiguration);
    }

//    @Bean
//    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory redisConnectionFactory) {
//        return new StringRedisTemplate(redisConnectionFactory);
//    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        template.setConnectionFactory(redisConnectionFactory());
        return template;
    }
}
