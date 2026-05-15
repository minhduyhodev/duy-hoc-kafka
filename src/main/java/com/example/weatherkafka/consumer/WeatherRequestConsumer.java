package com.example.weatherkafka.consumer;

import com.example.weatherkafka.config.KafkaTopicConfig;
import com.example.weatherkafka.model.WeatherRequest;
import com.example.weatherkafka.producer.WeatherResultProducer;
import com.example.weatherkafka.service.OpenMeteoWeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class WeatherRequestConsumer {

    private static final Logger log = LoggerFactory.getLogger(WeatherRequestConsumer.class);

    private final WeatherResultProducer weatherResultProducer;
    private final OpenMeteoWeatherService openMeteoWeatherService;

    public WeatherRequestConsumer(
            WeatherResultProducer weatherResultProducer,
            OpenMeteoWeatherService openMeteoWeatherService) {
        this.weatherResultProducer = weatherResultProducer;
        this.openMeteoWeatherService = openMeteoWeatherService;
    }

    @KafkaListener(topics = KafkaTopicConfig.WEATHER_REQUEST_TOPIC, groupId = "weather-request-worker")
    public void consumeRequest(WeatherRequest request) {
        System.out.println("[CONSUMER 1] Received weather request for city: " + request.getCity());

        try {
            weatherResultProducer.sendResult(openMeteoWeatherService.fetchForecast(request));
            System.out.println("[CONSUMER 1] Live forecast result sent to Kafka result topic");
        } catch (Exception ex) {
            log.error("Failed to fetch live forecast for city '{}'", request.getCity(), ex);
        }
    }
}
