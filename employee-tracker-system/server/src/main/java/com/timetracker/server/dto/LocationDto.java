package com.timetracker.server.dto;

import lombok.Data;

/**
 * DTO для проверки геолокации сотрудника.
 * Используется при попытке начать/закончить рабочий день.
 */
@Data
public class LocationDto {
    /**
     * Широта текущего местоположения телефона сотрудника.
     */
    private Double latitude;
    
    /**
     * Долгота текущего местоположения телефона сотрудника.
     */
    private Double longitude;
}
