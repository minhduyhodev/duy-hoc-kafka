package com.example.weatherkafka.service;

import com.example.weatherkafka.model.WeatherResult;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Service
public class WeatherResultFeedService {

    private static final int MAX_RESULTS = 20;

    private final Deque<WeatherResult> recentResults = new ArrayDeque<>();

    public synchronized void addResult(WeatherResult result) {
        recentResults.addFirst(result);

        while (recentResults.size() > MAX_RESULTS) {
            recentResults.removeLast();
        }
    }

    public synchronized List<WeatherResult> getRecentResults(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, MAX_RESULTS));
        return new ArrayList<>(recentResults).stream()
                .limit(safeLimit)
                .toList();
    }

    public synchronized int getRecentResultCount() {
        return recentResults.size();
    }
}
