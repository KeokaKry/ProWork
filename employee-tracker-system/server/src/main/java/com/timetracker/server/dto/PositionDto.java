package com.timetracker.server.dto;

import lombok.Data;

/**
 * DTO для передачи данных о должности.
 * Используется при создании и обновлении должностей администратором.
 */
@Data
public class PositionDto {
    /**
     * Название должности (например, "Электрик", "Уборщик").
     */
    private String name;
    
    /**
     * Тариф за час работы в рублях.
     * Администратор назначает этот тариф для расчета зарплаты.
     */
    private Double hourlyRate;
}
