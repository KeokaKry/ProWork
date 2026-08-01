package com.worktrcker.app.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * DTO для начала рабочего дня с геолокацией.
 * Сотрудник отправляет свои координаты при начале работы.
 */
@Data
public class StartWorkDayRequest {

    /**
     * Широта местоположения сотрудника
     */
    @NotNull(message = "Широта обязательна")
    private Double latitude;

    /**
     * Долгота местоположения сотрудника
     */
    @NotNull(message = "Долгота обязательна")
    private Double longitude;
}
