package com.worktrcker.app.dto;

import lombok.Data;

/**
 * DTO для ответа при входе сотрудника.
 */
@Data
public class LoginResponse {

    private Long id;
    private String username;
    private String fullName;
    private String position;
}
