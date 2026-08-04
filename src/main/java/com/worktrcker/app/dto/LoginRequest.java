package com.worktrcker.app.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String fullName;
    private String password;
}