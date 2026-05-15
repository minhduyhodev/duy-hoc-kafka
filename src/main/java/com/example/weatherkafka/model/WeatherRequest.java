package com.example.weatherkafka.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public class WeatherRequest {

    @NotBlank(message = "City must not be blank")
    private String city;

    @NotBlank(message = "RequestedBy must not be blank")
    private String requestedBy;

    @Email(message = "RecipientEmail must be a valid email address")
    private String recipientEmail;

    public WeatherRequest() {
    }

    public WeatherRequest(String city, String requestedBy, String recipientEmail) {
        this.city = city;
        this.requestedBy = requestedBy;
        this.recipientEmail = recipientEmail;
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
}
