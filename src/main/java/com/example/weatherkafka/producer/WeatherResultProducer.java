package com.example.weatherkafka.producer;

import com.example.weatherkafka.config.KafkaTopicConfig;
import com.example.weatherkafka.model.WeatherResult;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class WeatherResultProducer {

    private final KafkaTemplate<String, WeatherResult> kafkaTemplate;

    public WeatherResultProducer(KafkaTemplate<String, WeatherResult> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendResult(WeatherResult result) {
        kafkaTemplate.send(KafkaTopicConfig.WEATHER_RESULT_TOPIC, result.getCity(), result);
    }
}
