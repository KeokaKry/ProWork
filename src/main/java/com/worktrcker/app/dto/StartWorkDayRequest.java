package com.worktrcker.app.dto;

import lombok.Data;

/**
 * DTO для начала рабочего дня с геолокацией.
 * Сотрудник отправляет свои координаты при начале работы.
 */
@Data
public class StartWorkDayRequest {

    /**
     * Широта местоположения сотрудника
     */
    private Double latitude;

    /**
     * Долгота местоположения сотрудника
     */
    private Double longitude;
}
