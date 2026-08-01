package com.worktrcker.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность "Рабочий день"
 * Хранит информацию о рабочем дне сотрудника:
 * - время начала и окончания работы
 * - обеденный перерыв (начинается через 4 часа от начала работы)
 * - короткие перерывы (с 50 по 60 минуту каждого часа, кроме обеда)
 * - комментарий сотрудника о выполненной работе
 */
@Data
@Entity
@Table(name = "work_days")
public class WorkDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /** Дата рабочего дня */
    @Column(nullable = false)
    private LocalDate date;

    /** Время начала рабочего дня */
    private LocalTime startTime;

    /** Время окончания рабочего дня */
    private LocalTime endTime;

    /** Время начала обеда (автоматически: startTime + 4 часа) */
    private LocalTime lunchStart;

    /** Время окончания обеда (lunchStart + 1 час) */
    private LocalTime lunchEnd;

    /** 
     * Список коротких перерывов (с 50 по 60 минуту каждого часа)
     * Формат: "HH:MM-HH:MM" например "10:50-11:00"
     */
    @ElementCollection
    @CollectionTable(name = "breaks", joinColumns = @JoinColumn(name = "work_day_id"))
    @Column(name = "break_time")
    private List<String> shortBreaks = new ArrayList<>();

    /** Комментарий сотрудника о выполненной работе */
    @Column(length = 2000)
    private String comment;

    /** Статус рабочего дня */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkDayStatus status = WorkDayStatus.NOT_STARTED;

    /** Общее количество отработанных часов (без учета обеда) */
    private Double workedHours;

    /** Штраф за день (если назначен администратором) */
    private Double penalty;

    /** Причина штрафа */
    @Column(length = 500)
    private String penaltyReason;

    /** Дата создания записи */
    @Column(updatable = false)
    private LocalDate createdAt;

    /** Дата последнего обновления */
    private LocalDate updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
        updatedAt = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();
    }

    /** Статус рабочего дня */
    public enum WorkDayStatus {
        NOT_STARTED,      // День еще не начался
        WORKING,          // Сотрудник на работе
        ON_BREAK,         // На перерыве
        ON_LUNCH,         // На обеде
        FINISHED          // День завершен
    }
}
