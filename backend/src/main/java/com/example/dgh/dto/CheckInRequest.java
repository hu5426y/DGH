package com.example.dgh.dto;

import jakarta.validation.constraints.NotBlank;

public class CheckInRequest {
    @NotBlank
    private String technicianName;
    @NotBlank
    private String location;

    public String getTechnicianName() {
        return technicianName;
    }

    public void setTechnicianName(String technicianName) {
        this.technicianName = technicianName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
