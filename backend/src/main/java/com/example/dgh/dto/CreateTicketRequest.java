package com.example.dgh.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class CreateTicketRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    private List<String> images;
    @NotBlank
    private String locationId;
    @NotBlank
    private String reporterId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }
}
