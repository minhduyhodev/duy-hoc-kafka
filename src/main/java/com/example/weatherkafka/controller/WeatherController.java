package com.example.weatherkafka.controller;

import com.example.weatherkafka.config.KafkaTopicConfig;
import com.example.weatherkafka.model.WeatherRequest;
import com.example.weatherkafka.model.WeatherResult;
import com.example.weatherkafka.producer.WeatherRequestProducer;
import com.example.weatherkafka.service.WeatherResultFeedService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherRequestProducer weatherRequestProducer;
    private final WeatherResultFeedService weatherResultFeedService;
    private final boolean mailEnabled;
    private final String weatherProvider;

    public WeatherController(
            WeatherRequestProducer weatherRequestProducer,
            WeatherResultFeedService weatherResultFeedService,
            @Value("${app.mail.enabled:false}") boolean mailEnabled,
            @Value("${app.weather.provider:Open-Meteo}") String weatherProvider) {
        this.weatherRequestProducer = weatherRequestProducer;
        this.weatherResultFeedService = weatherResultFeedService;
        this.mailEnabled = mailEnabled;
        this.weatherProvider = weatherProvider;
    }

    @PostMapping("/forecast")
    public ResponseEntity<Map<String, String>> forecast(@Valid @RequestBody WeatherRequest request) {
        weatherRequestProducer.sendRequest(request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Weather request was sent to Kafka");
        response.put("city", request.getCity());
        response.put("note", "Open the Spring Boot console to see consumer logs and mail status");
        response.put("recipientEmail", request.getRecipientEmail() == null ? "" : request.getRecipientEmail());

        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Weather Kafka Demo is running");
    }

    @GetMapping("/results")
    public ResponseEntity<List<WeatherResult>> recentResults(
            @RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(weatherResultFeedService.getRecentResults(limit));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("application", "Weather Kafka Demo");
        response.put("weatherProvider", weatherProvider);
        response.put("mailEnabled", mailEnabled);
        response.put("requestTopic", KafkaTopicConfig.WEATHER_REQUEST_TOPIC);
        response.put("resultTopic", KafkaTopicConfig.WEATHER_RESULT_TOPIC);
        response.put("recentResults", weatherResultFeedService.getRecentResultCount());

        return ResponseEntity.ok(response);
    }
}
