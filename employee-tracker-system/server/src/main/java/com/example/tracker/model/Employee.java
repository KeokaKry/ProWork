package com.example.tracker.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Модель сотрудника.
 * Хранит информацию о работнике: имя, должность, привязку к геозоне.
 */
@Entity
@Table(name = "employees")
@Data
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Полное имя сотрудника (Фамилия Имя)
     */
    @Column(nullable = false)
    private String fullName;

    /**
     * Должность сотрудника
     */
    @Column(nullable = false)
    private String position;

    /**
     * Пароль (в реальном проекте нужно хешировать!)
     */
    @Column(nullable = false)
    private String password;

    /**
     * Геозона, привязанная к сотруднику.
     * Сотрудник может начать рабочий день только находясь в этой зоне.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "geo_zone_id")
    private GeoZone geoZone;

    /**
     * Дата и время создания записи
     */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
