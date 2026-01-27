package com.example.dgh.dto;

public class LocationOptionResponse {
    private String id;
    private String name;

    public LocationOptionResponse(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
