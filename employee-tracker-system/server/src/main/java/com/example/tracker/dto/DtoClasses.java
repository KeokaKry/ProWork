package com.example.tracker.dto;

import lombok.Data;
import java.time.LocalTime;

/**
 * DTO (Data Transfer Object) для регистрации нового сотрудника.
 * Используется для передачи данных от клиента к серверу при регистрации.
 */
@Data
public class RegisterRequest {
    /**
     * Полное имя сотрудника (Фамилия Имя)
     */
    private String fullName;
    
    /**
     * Должность сотрудника (например, "Электрик", "Сантехник")
     */
    private String position;
    
    /**
     * Пароль для входа в систему
     */
    private String password;
}

/**
 * DTO для ответа сервера при авторизации/регистрации.
 * Содержит токен и информацию о пользователе.
 */
@Data
public class AuthResponse {
    private Long id;
    private String fullName;
    private String position;
    private String token; // JWT токен или простой session ID
    
    public AuthResponse(Long id, String fullName, String position, String token) {
        this.id = id;
        this.fullName = fullName;
        this.position = position;
        this.token = token;
    }
}

/**
 * DTO для запроса начала рабочего дня.
 * Содержит координаты GPS телефона сотрудника.
 */
@Data
public class StartWorkRequest {
    /**
     * Широта местоположения сотрудника
     */
    private Double latitude;
    
    /**
     * Долгота местоположения сотрудника
     */
    private Double longitude;
}

/**
 * DTO для запроса завершения рабочего дня.
 * Содержит комментарий о проделанной работе.
 */
@Data
public class EndWorkRequest {
    /**
     * Текстовый комментарий о выполненной работе за день
     */
    private String comment;
}

/**
 * DTO для создания новой должности администратором.
 */
@Data
public class PositionRequest {
    /**
     * Название должности
     */
    private String name;
    
    /**
     * Ставка оплаты за час работы для этой должности
     */
    private Double hourlyRate;
}

/**
 * DTO для создания геозоны администратором.
 */
@Data
public class GeoZoneRequest {
    /**
     * Название объекта/офиса (например, "Офис Москва", "Склад №1")
     */
    private String name;
    
    /**
     * Широта центра геозоны
     */
    private Double latitude;
    
    /**
     * Долгота центра геозоны
     */
    private Double longitude;
    
    /**
     * Радиус геозоны в метрах (например, 50 метров)
     */
    private Integer radiusMeters;
}

/**
 * DTO для назначения геозоны сотруднику.
 */
@Data
public class AssignGeoZoneRequest {
    /**
     * ID сотрудника, которому назначается геозона
     */
    private Long employeeId;
    
    /**
     * ID геозоны, которая назначается сотруднику
     */
    private Long geoZoneId;
}

/**
 * DTO для назначения штрафа сотруднику.
 */
@Data
public class FineRequest {
    /**
     * Сумма штрафа в рублях
     */
    private Double amount;
    
    /**
     * Причина штрафа (комментарий администратора)
     */
    private String reason;
}

/**
 * DTO для создания задания на день недели.
 */
@Data
public class WeeklyTaskRequest {
    /**
     * ID сотрудника, которому назначается задание
     */
    private Long employeeId;
    
    /**
     * День недели (1-Пн, 2-Вт, ..., 7-Вс)
     */
    private Integer dayOfWeek;
    
    /**
     * Текст задания
     */
    private String taskDescription;
}
