package com.worktrcker.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность "Задание на день недели"
 * Администратор создает задания для сотрудников по дням недели
 */
@Data
@Entity
@Table(name = "weekly_tasks")
public class WeeklyTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /** День недели, к которому относится задание */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    /** Текст задания */
    @Column(length = 2000, nullable = false)
    private String taskDescription;

    /** Активно ли задание */
    @Column(nullable = false)
    private boolean active = true;

    /** Дата создания задания */
    @Column(updatable = false)
    private java.time.LocalDate createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = java.time.LocalDate.now();
    }
}
