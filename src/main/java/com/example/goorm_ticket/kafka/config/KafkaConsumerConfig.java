package com.example.goorm_ticket.kafka.config;

import com.example.goorm_ticket.domain.coupon.dto.CouponEventDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@Slf4j
public class KafkaConsumerConfig {
    private final Environment env;

    KafkaConsumerConfig(Environment env) {
        this.env = env;
    }

    public Map<String, Object> consumerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,env.getProperty("spring.kafka.consumer.bootstrap-servers"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG,env.getProperty("spring.kafka.consumer.group-id"));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,env.getProperty("spring.kafka.consumer.auto-offset-reset"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(org.springframework.kafka.support.serializer.JsonDeserializer.TYPE_MAPPINGS, "CouponEventDto:com.example.goorm_ticket.domain.coupon.dto.CouponEventDto");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        // props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");
        // props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, Arrays.asList(StickyAssignor.class));
        return props;
    }
    @Bean
    public ConsumerFactory<String, CouponEventDto> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(this.consumerConfig());
    }

    @Bean
    public DeadLetterPublishingRecoverer recoverer(KafkaTemplate<String, CouponEventDto> kafkaTemplate) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition()));
    }
    @Bean
    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer recoverer) {
        FixedBackOff fixedBackOff = new FixedBackOff(1000L, 3);
        return new DefaultErrorHandler(recoverer, fixedBackOff);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CouponEventDto> kafkaListenerContainerFactory(DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, CouponEventDto> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(this.consumerFactory());
        //factory.setConcurrency(3); // consumer 개수 설정, 파티션 개수도 3개로 함
        factory.setBatchListener(true); // 메시지 소비 배치 처리
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
