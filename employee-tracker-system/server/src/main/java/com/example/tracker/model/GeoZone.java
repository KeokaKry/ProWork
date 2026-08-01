package com.example.tracker.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Модель геозоны.
 * Определяет территорию (объект/офис) с центром в координатах и радиусом.
 */
@Entity
@Table(name = "geo_zones")
@Data
public class GeoZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название геозоны (например, "Офис Москва", "Склад №1")
     */
    @Column(nullable = false)
    private String name;

    /**
     * Широта центра геозоны (GPS координата)
     */
    @Column(nullable = false)
    private Double latitude;

    /**
     * Долгота центра геозоны (GPS координата)
     */
    @Column(nullable = false)
    private Double longitude;

    /**
     * Радиус геозоны в метрах.
     * Сотрудник должен находиться внутри этого радиуса для начала работы.
     */
    @Column(nullable = false)
    private Integer radiusMeters;
}
