package com.timetracker.server.dto;

import lombok.Data;

/**
 * DTO для передачи информации о сотруднике.
 * Используется в ответе администратору.
 */
@Data
public class EmployeeDto {
    /**
     * ID сотрудника.
     */
    private Long id;
    
    /**
     * Имя пользователя (логин).
     */
    private String username;
    
    /**
     * Должность.
     */
    private String position;
    
    /**
     * ID геозоны, к которой привязан сотрудник.
     */
    private Long geoZoneId;
    
    /**
     * Название геозоны.
     */
    private String geoZoneName;
}
