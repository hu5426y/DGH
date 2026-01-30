package com.example.dgh.dto;

import java.util.List;

public class OpsOverviewResponse {
    private int slaHours;
    private long overdueCount;
    private double slaComplianceRate;
    private List<AreaLeadResponse> areaLeads;
    private List<TechnicianPerformanceResponse> technicianPerformance;
    private List<IssueStatResponse> issueStats;
    private long pendingFeedbackCount;
    private long escalatedCount;

    public OpsOverviewResponse(
        int slaHours,
        long overdueCount,
        double slaComplianceRate,
        List<AreaLeadResponse> areaLeads,
        List<TechnicianPerformanceResponse> technicianPerformance,
        List<IssueStatResponse> issueStats,
        long pendingFeedbackCount,
        long escalatedCount
    ) {
        this.slaHours = slaHours;
        this.overdueCount = overdueCount;
        this.slaComplianceRate = slaComplianceRate;
        this.areaLeads = areaLeads;
        this.technicianPerformance = technicianPerformance;
        this.issueStats = issueStats;
        this.pendingFeedbackCount = pendingFeedbackCount;
        this.escalatedCount = escalatedCount;
    }

    public int getSlaHours() {
        return slaHours;
    }

    public long getOverdueCount() {
        return overdueCount;
    }

    public double getSlaComplianceRate() {
        return slaComplianceRate;
    }

    public List<AreaLeadResponse> getAreaLeads() {
        return areaLeads;
    }

    public List<TechnicianPerformanceResponse> getTechnicianPerformance() {
        return technicianPerformance;
    }

    public List<IssueStatResponse> getIssueStats() {
        return issueStats;
    }

    public long getPendingFeedbackCount() {
        return pendingFeedbackCount;
    }

    public long getEscalatedCount() {
        return escalatedCount;
    }
}
