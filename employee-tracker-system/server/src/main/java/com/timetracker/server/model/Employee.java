package com.timetracker.server.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность Сотрудник.
 * Хранит данные пользователя, его должность и привязку к рабочей геозоне.
 */
@Entity
@Data
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    /**
     * Должность сотрудника (например, "Электрик", "Уборщик").
     * Используется для расчета тарифа администратором.
     */
    @Column(nullable = false)
    private String position;

    /**
     * Привязка к геозоне (объекту), где сотрудник должен работать.
     * Администратор назначает эту зону через свою панель.
     * Сотрудник может отмечаться только в пределах этой зоны.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "geo_zone_id")
    private GeoZone geoZone;

    // Связь с рабочими днями (история посещений)
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkDay> workDays = new ArrayList<>();
}
