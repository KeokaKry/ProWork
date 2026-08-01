package com.worktrcker.app.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Сущность "Геозона" (объект/офис).
 * Администратор создает геозоны на карте с радиусом.
 * Сотрудник может начать рабочий день только находясь внутри геозоны.
 */
@Data
@Entity
@Table(name = "geo_zones")
public class GeoZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Название объекта (например, "Офис Москва", "Склад №3") */
    @Column(nullable = false)
    private String name;

    /** Широта центра геозоны */
    @Column(nullable = false)
    private Double latitude;

    /** Долгота центра геозоны */
    @Column(nullable = false)
    private Double longitude;

    /** Радиус геозоны в метрах (например, 50 метров) */
    @Column(nullable = false)
    private Integer radiusMeters = 50;

    /** Активна ли геозона */
    @Column(nullable = false)
    private boolean active = true;
}
