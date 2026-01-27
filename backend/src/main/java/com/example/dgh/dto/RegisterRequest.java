package com.example.dgh.dto;

import jakarta.validation.constraints.NotBlank;

public class RegisterRequest {
    @NotBlank
    private String userId;
    @NotBlank
    private String phone;
    @NotBlank
    private String password;
    @NotBlank
    private String name;
    @NotBlank
    private String roleCode;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }
}
