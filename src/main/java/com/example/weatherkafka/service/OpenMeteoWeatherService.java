package com.example.weatherkafka.service;

import com.example.weatherkafka.model.WeatherRequest;
import com.example.weatherkafka.model.WeatherResult;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OpenMeteoWeatherService {

    private final RestClient restClient;
    private final String geocodingBaseUrl;
    private final String forecastBaseUrl;
    private final String geocodingLanguage;
    private final String geocodingCountryCode;

    public OpenMeteoWeatherService(
            RestClient.Builder restClientBuilder,
            @Value("${app.weather.geocoding-base-url:https://geocoding-api.open-meteo.com}") String geocodingBaseUrl,
            @Value("${app.weather.forecast-base-url:https://api.open-meteo.com}") String forecastBaseUrl,
            @Value("${app.weather.geocoding-language:en}") String geocodingLanguage,
            @Value("${app.weather.geocoding-country-code:}") String geocodingCountryCode) {
        this.restClient = restClientBuilder.build();
        this.geocodingBaseUrl = geocodingBaseUrl;
        this.forecastBaseUrl = forecastBaseUrl;
        this.geocodingLanguage = geocodingLanguage;
        this.geocodingCountryCode = geocodingCountryCode;
    }

    public WeatherResult fetchForecast(WeatherRequest request) {
        GeocodingResult location = resolveLocation(request.getCity());
        ForecastCurrent current = fetchCurrentForecast(location);

        return new WeatherResult(
                formatLocationName(location),
                request.getRequestedBy(),
                request.getRecipientEmail(),
                (int) Math.round(current.temperature2m()),
                mapWeatherCode(current.weatherCode()),
                parseForecastTime(current.time())
        );
    }

    private GeocodingResult resolveLocation(String city) {
        URI uri = UriComponentsBuilder.fromHttpUrl(geocodingBaseUrl + "/v1/search")
                .queryParam("name", city)
                .queryParam("count", 1)
                .queryParam("language", geocodingLanguage)
                .queryParam("format", "json")
                .queryParamIfPresent("countryCode", geocodingCountryCode.isBlank() ? java.util.Optional.empty() : java.util.Optional.of(geocodingCountryCode))
                .build()
                .encode()
                .toUri();

        GeocodingResponse response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(GeocodingResponse.class);

        if (response == null || response.results() == null || response.results().isEmpty()) {
            throw new IllegalArgumentException("No location found for city: " + city);
        }

        return response.results().getFirst();
    }

    private ForecastCurrent fetchCurrentForecast(GeocodingResult location) {
        URI uri = UriComponentsBuilder.fromHttpUrl(forecastBaseUrl + "/v1/forecast")
                .queryParam("latitude", location.latitude())
                .queryParam("longitude", location.longitude())
                .queryParam("current", "temperature_2m,weather_code")
                .queryParam("timezone", location.timezone() == null || location.timezone().isBlank() ? "auto" : location.timezone())
                .build()
                .encode()
                .toUri();

        ForecastResponse response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(ForecastResponse.class);

        if (response == null || response.current() == null) {
            throw new IllegalStateException("Weather forecast API returned no current forecast data");
        }

        return response.current();
    }

    private String formatLocationName(GeocodingResult location) {
        List<String> parts = new ArrayList<>();
        String cityName = safeText(location.name());
        parts.add(cityName);

        String admin1 = safeText(location.admin1());
        String country = safeText(location.country());

        if (isDistinctLocationPart(cityName, admin1)) {
            parts.add(admin1);
        }
        if (!country.isBlank()) {
            parts.add(country);
        }

        return String.join(", ", parts);
    }

    private LocalDateTime parseForecastTime(String time) {
        return LocalDateTime.parse(time);
    }

    private String mapWeatherCode(int weatherCode) {
        return switch (weatherCode) {
            case 0 -> "Clear sky";
            case 1 -> "Mainly clear";
            case 2 -> "Partly cloudy";
            case 3 -> "Overcast";
            case 45, 48 -> "Fog";
            case 51, 53, 55 -> "Drizzle";
            case 56, 57 -> "Freezing drizzle";
            case 61, 63, 65 -> "Rain";
            case 66, 67 -> "Freezing rain";
            case 71, 73, 75 -> "Snow";
            case 77 -> "Snow grains";
            case 80, 81, 82 -> "Rain showers";
            case 85, 86 -> "Snow showers";
            case 95 -> "Thunderstorm";
            case 96, 99 -> "Thunderstorm with hail";
            default -> "Weather code " + weatherCode;
        };
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isDistinctLocationPart(String cityName, String admin1) {
        if (admin1.isBlank()) {
            return false;
        }

        String normalizedCity = normalizeForComparison(cityName);
        String normalizedAdmin = normalizeForComparison(admin1);

        if (normalizedAdmin.isBlank()) {
            return false;
        }

        return !normalizedAdmin.equals(normalizedCity)
                && !normalizedAdmin.contains(normalizedCity)
                && !normalizedCity.contains(normalizedAdmin);
    }

    private String normalizeForComparison(String value) {
        return safeText(value)
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeocodingResponse(List<GeocodingResult> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeocodingResult(
            String name,
            double latitude,
            double longitude,
            String timezone,
            String country,
            String admin1) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ForecastResponse(ForecastCurrent current) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ForecastCurrent(
            String time,
            @JsonProperty("temperature_2m") double temperature2m,
            @JsonProperty("weather_code") int weatherCode) {
    }
}
