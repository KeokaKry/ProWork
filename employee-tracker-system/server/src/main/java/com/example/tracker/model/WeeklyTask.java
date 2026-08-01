package com.example.tracker.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Модель еженедельного задания.
 * Задание от администратора сотруднику на конкретный день недели.
 */
@Entity
@Table(name = "weekly_tasks")
@Data
public class WeeklyTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Сотрудник, которому назначено задание
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * День недели (1-Пн, 2-Вт, ..., 7-Вс)
     */
    @Column(nullable = false)
    private Integer dayOfWeek;

    /**
     * Текст задания
     */
    @Column(nullable = false, length = 1000)
    private String description;

    /**
     * Дата и время создания задания
     */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
