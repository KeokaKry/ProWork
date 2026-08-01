package com.timetracker.server.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность Рабочий День.
 * Хранит информацию о начале и конце рабочего дня, перерывах, обеде,
 * фотографиях выполненной работы, комментариях и штрафах.
 */
@Entity
@Data
@Table(name = "work_days")
public class WorkDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Сотрудник, которому принадлежит этот рабочий день.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Дата рабочего дня.
     */
    @Column(nullable = false)
    private LocalDate date;

    /**
     * Время начала рабочего дня.
     */
    @Column(nullable = false)
    private LocalDateTime startTime;

    /**
     * Время окончания рабочего дня.
     */
    @Column
    private LocalDateTime endTime;

    /**
     * Время начала обеденного перерыва (автоматически: через 4 часа после начала).
     */
    @Column
    private LocalDateTime lunchStartTime;

    /**
     * Время окончания обеденного перерыва (1 час после начала обеда).
     */
    @Column
    private LocalDateTime lunchEndTime;

    /**
     * Список коротких перерывов (с 50 по 60 минуту каждого часа, кроме обеда).
     * Формат: "HH:mm-HH:mm" (например, "10:50-11:00").
     */
    @ElementCollection
    @CollectionTable(name = "short_breaks", joinColumns = @JoinColumn(name = "work_day_id"))
    @Column(name = "break_time")
    private List<String> shortBreaks = new ArrayList<>();

    /**
     * Комментарий сотрудника о выполненной работе за день.
     */
    @Column(length = 2000)
    private String comment;

    /**
     * Штраф, наложенный администратором за этот день (в рублях).
     */
    @Column
    private Double penalty;

    /**
     * Фотографии выполненной работы, загруженные сотрудником.
     */
    @OneToMany(mappedBy = "workDay", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkPhoto> photos = new ArrayList<>();

    /**
     * Статус рабочего дня (активен/завершен).
     */
    @Column(nullable = false)
    private boolean active = true;
}
