package com.example.dgh.dto;

public class InsightSummaryResponse {
    private String highFrequencyArea;
    private int averageResponseMinutes;
    private int nightShiftPending;

    public InsightSummaryResponse(String highFrequencyArea, int averageResponseMinutes, int nightShiftPending) {
        this.highFrequencyArea = highFrequencyArea;
        this.averageResponseMinutes = averageResponseMinutes;
        this.nightShiftPending = nightShiftPending;
    }

    public String getHighFrequencyArea() {
        return highFrequencyArea;
    }

    public int getAverageResponseMinutes() {
        return averageResponseMinutes;
    }

    public int getNightShiftPending() {
        return nightShiftPending;
    }
}
