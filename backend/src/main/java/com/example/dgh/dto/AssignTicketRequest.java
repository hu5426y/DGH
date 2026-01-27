package com.example.dgh.dto;

import jakarta.validation.constraints.NotBlank;

public class AssignTicketRequest {
    @NotBlank
    private String assignedToName;
    private String assignedToContact;

    public String getAssignedToName() {
        return assignedToName;
    }

    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
    }

    public String getAssignedToContact() {
        return assignedToContact;
    }

    public void setAssignedToContact(String assignedToContact) {
        this.assignedToContact = assignedToContact;
    }
}
