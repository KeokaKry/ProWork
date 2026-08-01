package com.worktrcker.app.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * Сущность "Сотрудник".
 * Хранит информацию о сотруднике: ФИО, должность, ставку, телефон и логин.
 */
@Data
@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Полное имя сотрудника (Фамилия Имя) */
    private String fullName;
    
    /** Должность (например, "Электрик", "Уборщик") */
    private String position;
    
    /** Ставка в час (устанавливается администратором) */
    private Double hourlyRate;
    
    /** Номер телефона для связи */
    private String phoneNumber;
    
    /** Логин для входа в систему (username) */
    private String username;
    
    /** Пароль (в реальном проекте нужно хешировать) */
    private String password;
}