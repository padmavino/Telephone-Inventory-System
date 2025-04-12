package com.telecom.inventory.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.topics.file-processing}")
    private String fileProcessingTopic;

    @Value("${app.kafka.topics.number-allocation}")
    private String numberAllocationTopic;

    @Value("${app.kafka.topics.status-change}")
    private String statusChangeTopic;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic fileProcessingTopic() {
        return TopicBuilder.name(fileProcessingTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic numberAllocationTopic() {
        return TopicBuilder.name(numberAllocationTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic statusChangeTopic() {
        return TopicBuilder.name(statusChangeTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
