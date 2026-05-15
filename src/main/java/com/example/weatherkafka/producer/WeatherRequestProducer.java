package com.example.weatherkafka.producer;

import com.example.weatherkafka.config.KafkaTopicConfig;
import com.example.weatherkafka.model.WeatherRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class WeatherRequestProducer {

    private final KafkaTemplate<String, WeatherRequest> kafkaTemplate;

    public WeatherRequestProducer(KafkaTemplate<String, WeatherRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendRequest(WeatherRequest request) {
        kafkaTemplate.send(KafkaTopicConfig.WEATHER_REQUEST_TOPIC, request.getCity(), request);
    }
}
