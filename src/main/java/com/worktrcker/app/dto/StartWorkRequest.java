package com.worktrcker.app.dto;

import lombok.Data;

@Data
public class StartWorkRequest {
    private Long employeeId;
    private Double latitude;
    private Double longitude;
}