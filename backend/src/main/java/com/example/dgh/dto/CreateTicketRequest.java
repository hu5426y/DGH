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
    private String location;
    @NotBlank
    private String reporterName;
    @NotBlank
    private String reporterContact;

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReporterContact() {
        return reporterContact;
    }

    public void setReporterContact(String reporterContact) {
        this.reporterContact = reporterContact;
    }
}
