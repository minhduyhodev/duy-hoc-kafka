package com.example.weatherkafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String WEATHER_REQUEST_TOPIC = "weather-request-topic";
    public static final String WEATHER_RESULT_TOPIC = "weather-result-topic";

    @Bean
    public NewTopic weatherRequestTopic() {
        return TopicBuilder.name(WEATHER_REQUEST_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic weatherResultTopic() {
        return TopicBuilder.name(WEATHER_RESULT_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
