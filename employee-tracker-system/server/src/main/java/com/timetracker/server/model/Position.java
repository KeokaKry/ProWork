package com.timetracker.server.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Сущность Должность.
 * Хранит название должности и тариф за час работы.
 * Администратор назначает тарифы для автоматического расчета зарплаты.
 */
@Entity
@Data
@Table(name = "positions")
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название должности (например, "Электрик", "Уборщик", "Грузчик").
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Тариф за час работы в рублях.
     * Используется для автоматического расчета зарплаты сотрудника.
     */
    @Column(nullable = false)
    private Double hourlyRate;
}
