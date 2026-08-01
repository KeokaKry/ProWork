package com.timetracker.server.dto;

import lombok.Data;

/**
 * DTO для регистрации нового сотрудника.
 * Используется сотрудником при первой регистрации в приложении.
 */
@Data
public class RegisterDto {
    /**
     * Имя пользователя (логин).
     */
    private String username;
    
    /**
     * Пароль.
     */
    private String password;
    
    /**
     * Должность сотрудника.
     */
    private String position;
}
