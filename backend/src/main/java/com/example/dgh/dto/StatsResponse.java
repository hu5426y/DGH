package com.example.dgh.dto;

import java.util.Map;

public class StatsResponse {
    private long total;
    private long completed;
    private double avgRating;
    private Map<String, Long> frequentLocations;

    public StatsResponse(long total, long completed, double avgRating, Map<String, Long> frequentLocations) {
        this.total = total;
        this.completed = completed;
        this.avgRating = avgRating;
        this.frequentLocations = frequentLocations;
    }

    public long getTotal() {
        return total;
    }

    public long getCompleted() {
        return completed;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public Map<String, Long> getFrequentLocations() {
        return frequentLocations;
    }
}
