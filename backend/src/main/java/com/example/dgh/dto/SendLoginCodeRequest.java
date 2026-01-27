package com.example.dgh.dto;

import jakarta.validation.constraints.NotBlank;

public class SendLoginCodeRequest {
    @NotBlank
    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
