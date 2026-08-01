package com.worktrcker.app.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * DTO (Объект передачи данных) для регистрации нового сотрудника.
 * Используется для приема данных из JSON запроса от PWA приложения.
 */
@Data
public class RegisterRequest {

    /**
     * Полное имя сотрудника (Фамилия Имя)
     */
    @NotBlank(message = "Имя обязательно")
    private String fullName;

    /**
     * Должность сотрудника (например, "Электрик", "Уборщик")
     * От должности зависит тарифная ставка в будущем
     */
    @NotBlank(message = "Должность обязательна")
    private String position;

    /**
     * Логин для входа в систему (номер телефона)
     */
    @NotBlank(message = "Логин обязателен")
    private String username;

    /**
     * Пароль для входа
     */
    @NotBlank(message = "Пароль обязателен")
    private String password;
}
