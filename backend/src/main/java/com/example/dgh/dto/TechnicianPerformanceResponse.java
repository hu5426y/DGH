package com.example.dgh.dto;

public class TechnicianPerformanceResponse {
    private String technicianName;
    private long totalTickets;
    private long completedTickets;
    private int averageCompletionMinutes;

    public TechnicianPerformanceResponse(String technicianName, long totalTickets, long completedTickets, int averageCompletionMinutes) {
        this.technicianName = technicianName;
        this.totalTickets = totalTickets;
        this.completedTickets = completedTickets;
        this.averageCompletionMinutes = averageCompletionMinutes;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public long getTotalTickets() {
        return totalTickets;
    }

    public long getCompletedTickets() {
        return completedTickets;
    }

    public int getAverageCompletionMinutes() {
        return averageCompletionMinutes;
    }
}
