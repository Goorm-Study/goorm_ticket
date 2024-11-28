package com.example.goorm_ticket.kafka.config;

import com.example.goorm_ticket.domain.coupon.dto.CouponEventDto;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {
    private final Environment env;

    KafkaProducerConfig(Environment environment) {
        this.env = environment;
    }

    public Map<String, Object> producerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                env.getProperty("spring.kafka.producer.bootstrap-servers"));

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // replication factor에 맞게 토픽의 min in sync replicas 설정 필요
        props.put(org.springframework.kafka.support.serializer.JsonSerializer.TYPE_MAPPINGS, "CouponEventDto:com.example.goorm_ticket.domain.coupon.dto.CouponEventDto");
        return props;
    }

    @Bean
    public ProducerFactory<String, CouponEventDto> producerFactory() {
        return new DefaultKafkaProducerFactory<>(this.producerConfig());
    }

    @Bean
    public KafkaTemplate<String, CouponEventDto> kafkaTemplate() {
        return new KafkaTemplate<>(this.producerFactory());
    }
}
