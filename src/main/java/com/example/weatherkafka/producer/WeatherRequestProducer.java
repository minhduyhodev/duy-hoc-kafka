package com.example.weatherkafka.producer;

import com.example.weatherkafka.config.KafkaTopicConfig;
import com.example.weatherkafka.model.WeatherRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class WeatherRequestProducer {

    private static final Logger log = LoggerFactory.getLogger(WeatherRequestProducer.class);

    private final KafkaTemplate<String, WeatherRequest> kafkaTemplate;

    public WeatherRequestProducer(KafkaTemplate<String, WeatherRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendRequest(WeatherRequest request) {
        kafkaTemplate.send(KafkaTopicConfig.WEATHER_REQUEST_TOPIC, request.getCity(), request)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to enqueue weather request for city '{}'", request.getCity(), ex);
                        return;
                    }

                    log.info("Enqueued weather request for city '{}' to topic '{}'",
                            request.getCity(),
                            KafkaTopicConfig.WEATHER_REQUEST_TOPIC);
                });
    }
}
