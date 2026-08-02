package com.worktrcker.app.dto;

import lombok.Data;

/**
 * DTO для входа сотрудника по ФИО и паролю.
 * Пароль выдается администратором при регистрации сотрудника.
 */
@Data
public class LoginRequest {

    private String fullName;
    private String password;
}
