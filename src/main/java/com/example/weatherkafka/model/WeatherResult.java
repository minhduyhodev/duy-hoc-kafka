package com.example.weatherkafka.model;

import java.time.LocalDateTime;

public class WeatherResult {

    private String city;
    private String requestedBy;
    private String recipientEmail;
    private int temperature;
    private String status;
    private LocalDateTime forecastTime;

    public WeatherResult() {
    }

    public WeatherResult(String city, String requestedBy, String recipientEmail, int temperature, String status, LocalDateTime forecastTime) {
        this.city = city;
        this.requestedBy = requestedBy;
        this.recipientEmail = recipientEmail;
        this.temperature = temperature;
        this.status = status;
        this.forecastTime = forecastTime;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getForecastTime() {
        return forecastTime;
    }

    public void setForecastTime(LocalDateTime forecastTime) {
        this.forecastTime = forecastTime;
    }
}
