package com.timetracker.server.dto;

import lombok.Data;

/**
 * DTO для назначения сотруднику должности и геозоны.
 * Используется администратором при привязке сотрудника к объекту.
 */
@Data
public class AssignmentDto {
    /**
     * ID должности, которую нужно назначить сотруднику.
     */
    private Long positionId;
    
    /**
     * ID геозоны (объекта), к которой нужно привязать сотрудника.
     */
    private Long geoZoneId;
}
