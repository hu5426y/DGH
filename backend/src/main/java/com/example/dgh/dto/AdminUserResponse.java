package com.example.dgh.dto;

import com.example.dgh.entity.UserRole;
import java.time.LocalDateTime;

public class AdminUserResponse {
    private String id;
    private String userId;
    private String phone;
    private String name;
    private UserRole roleCode;
    private Integer isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AdminUserResponse(String id, String userId, String phone, String name, UserRole roleCode, Integer isActive,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.phone = phone;
        this.name = name;
        this.roleCode = roleCode;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    public UserRole getRoleCode() {
        return roleCode;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
