package com.example.dgh.dto;

public class LoginResponse {
    private String token;
    private String id;
    private String userId;
    private String name;
    private String phone;
    private String roleCode;

    public LoginResponse(String token, String id, String userId, String name, String phone, String roleCode) {
        this.token = token;
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.roleCode = roleCode;
    }

    public String getToken() {
        return token;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getRoleCode() {
        return roleCode;
    }
}
