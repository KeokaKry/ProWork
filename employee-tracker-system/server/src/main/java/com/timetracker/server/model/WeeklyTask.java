package com.timetracker.server.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * Сущность Задание на день недели.
 * Администратор создает задания для сотрудников на определенные дни недели.
 */
@Entity
@Data
@Table(name = "weekly_tasks")
public class WeeklyTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Должность, для которой предназначено задание.
     * Задание будет видно всем сотрудникам этой должности.
     */
    @Column(nullable = false)
    private String position;

    /**
     * День недели (1-7, где 1=Понедельник, 7=Воскресенье).
     */
    @Column(nullable = false)
    private Integer dayOfWeek;

    /**
     * Текст задания.
     */
    @Column(nullable = false, length = 2000)
    private String taskDescription;

    /**
     * Активность задания (можно временно отключить).
     */
    @Column(nullable = false)
    private boolean active = true;
}
