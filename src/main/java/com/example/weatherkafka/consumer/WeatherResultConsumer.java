package com.example.weatherkafka.consumer;

import com.example.weatherkafka.config.KafkaTopicConfig;
import com.example.weatherkafka.model.WeatherResult;
import com.example.weatherkafka.service.EmailNotificationService;
import com.example.weatherkafka.service.WeatherResultFeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class WeatherResultConsumer {

    private static final Logger log = LoggerFactory.getLogger(WeatherResultConsumer.class);

    private final EmailNotificationService emailNotificationService;
    private final WeatherResultFeedService weatherResultFeedService;

    public WeatherResultConsumer(
            EmailNotificationService emailNotificationService,
            WeatherResultFeedService weatherResultFeedService) {
        this.emailNotificationService = emailNotificationService;
        this.weatherResultFeedService = weatherResultFeedService;
    }

    @KafkaListener(topics = KafkaTopicConfig.WEATHER_RESULT_TOPIC, groupId = "weather-result-printer")
    public void consumeResult(WeatherResult result) {
        try {
            System.out.println("========== WEATHER RESULT ==========");
            System.out.println("City: " + result.getCity());
            System.out.println("Requested by: " + result.getRequestedBy());
            System.out.println("Temperature: " + result.getTemperature() + " deg C");
            System.out.println("Status: " + result.getStatus());
            System.out.println("Forecast time: " + result.getForecastTime());
            if (result.getRecipientEmail() != null && !result.getRecipientEmail().isBlank()) {
                System.out.println("Recipient email: " + result.getRecipientEmail());
            }
            System.out.println("====================================");

            weatherResultFeedService.addResult(result);
            emailNotificationService.sendWeatherForecast(result);
        } catch (Exception ex) {
            log.error(
                    "Failed to process weather result for city '{}' and recipient '{}'",
                    result != null ? result.getCity() : null,
                    result != null ? result.getRecipientEmail() : null,
                    ex
            );
        }
    }
}
