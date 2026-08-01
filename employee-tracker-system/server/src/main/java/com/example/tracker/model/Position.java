package com.example.tracker.model;

import lombok.Data;

/**
 * Модель должности.
 * В упрощенной версии хранится в памяти, в реальной - в БД.
 */
@Data
public class Position {

    /**
     * ID должности
     */
    private Long id;

    /**
     * Название должности (например, "Электрик", "Сантехник")
     */
    private String name;

    /**
     * Ставка оплаты за час работы
     */
    private Double hourlyRate;
}
