package com.timetracker.server.dto;

import lombok.Data;

/**
 * DTO для создания геозоны администратором.
 * Содержит координаты центра и радиус зоны.
 */
@Data
public class GeoZoneDto {
    /**
     * Название геозоны (объекта/офиса).
     */
    private String name;
    
    /**
     * Широта центра геозоны.
     */
    private Double latitude;
    
    /**
     * Долгота центра геозоны.
     */
    private Double longitude;
    
    /**
     * Радиус геозоны в метрах (например, 50 метров).
     */
    private Integer radiusMeters;
}
