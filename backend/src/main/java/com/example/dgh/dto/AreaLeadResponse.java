package com.example.dgh.dto;

public class AreaLeadResponse {
    private String area;
    private long ticketCount;
    private String suggestedLead;

    public AreaLeadResponse(String area, long ticketCount, String suggestedLead) {
        this.area = area;
        this.ticketCount = ticketCount;
        this.suggestedLead = suggestedLead;
    }

    public String getArea() {
        return area;
    }

    public long getTicketCount() {
        return ticketCount;
    }

    public String getSuggestedLead() {
        return suggestedLead;
    }
}
