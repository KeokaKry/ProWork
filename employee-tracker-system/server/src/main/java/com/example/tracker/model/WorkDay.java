package com.example.tracker.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Модель рабочего дня.
 * Хранит информацию о начале, конце, перерывах и обеде сотрудника.
 */
@Entity
@Table(name = "work_days")
@Data
public class WorkDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Сотрудник, которому принадлежит рабочий день
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Дата рабочего дня
     */
    @Column(nullable = false)
    private java.time.LocalDate date;

    /**
     * Время начала работы
     */
    @Column(nullable = false)
    private LocalDateTime startTime;

    /**
     * Время окончания работы (null если смена еще не завершена)
     */
    private LocalDateTime endTime;

    /**
     * Время начала обеда (через 4 часа после начала смены)
     */
    private java.time.LocalTime lunchStartTime;

    /**
     * Время окончания обеда (через 5 часов после начала смены, т.е. 1 час обеда)
     */
    private java.time.LocalTime lunchEndTime;

    /**
     * Комментарий сотрудника о проделанной работе
     */
    @Column(length = 1000)
    private String comment;

    /**
     * Штраф, наложенный администратором
     */
    private Double penalty;

    /**
     * Дата и время создания записи
     */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // Автоматически устанавливаем время обеда при создании
        if (startTime != null) {
            this.lunchStartTime = startTime.toLocalTime().plusHours(4);
            this.lunchEndTime = this.lunchStartTime.plusMinutes(60);
        }
    }

    /**
     * Расчет общей продолжительности рабочего дня в часах (без обеда).
     * Перерывы по 10 минут каждый час (с 50 по 60 минуту) не вычитаются автоматически,
     * но учитываются при расчете зарплаты.
     */
    public Double getTotalHours() {
        if (startTime == null || endTime == null) {
            return 0.0;
        }

        long totalMinutes = java.time.Duration.between(startTime, endTime).toMinutes();

        // Вычитаем обед (1 час = 60 минут)
        if (lunchStartTime != null && lunchEndTime != null) {
            totalMinutes -= 60;
        }

        return totalMinutes / 60.0;
    }

    /**
     * Получение следующего времени перерыва.
     * Перерывы: с 50 по 60 минуту каждого часа (кроме времени обеда).
     */
    public java.time.LocalTime getNextBreakTime() {
        if (startTime == null) {
            return null;
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalTime currentHour = now.toLocalTime().withMinute(0).withSecond(0).withNano(0);

        // Ищем ближайший перерыв (50-60 минута часа)
        for (int i = 0; i < 12; i++) {
            java.time.LocalTime breakStart = currentHour.plusMinutes(50);
            java.time.LocalTime breakEnd = currentHour.plusMinutes(60);

            // Проверяем, не попадает ли перерыв на обед
            if (!(breakStart.isAfter(lunchStartTime) && breakEnd.isBefore(lunchEndTime.plusMinutes(1)))) {
                if (breakStart.isAfter(now.toLocalTime())) {
                    return breakStart;
                }
            }
            currentHour = currentHour.plusHours(1);
        }

        return null;
    }
}
