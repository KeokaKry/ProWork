package com.worktrcker.app.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

// @Data (от Lombok) автоматически создаст геттеры, сеттеры, toString и equals
// Тебе не нужно писать их вручную! Это экономит кучу времени.
@Data
// @Entity говорит Spring: "Этот класс нужно превратить в таблицу в базе данных"
@Entity
public class Employee {

    // @Id говорит, что это первичный ключ (уникальный идентификатор)
    // GenerationType.IDENTITY означает, что база данных сама будет придумывать номера (1, 2, 3...)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;       // ФИО сотрудника
    private String position;       // Должность (например, "Электрик")
    private Double hourlyRate;     // Ставка в час (например, 500.0)
    private String phoneNumber;    // Телефон (для связи и логина)
}