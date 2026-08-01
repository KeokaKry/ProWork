package com.timetracker.server.repository;

import com.timetracker.server.model.WeeklyTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с недельными заданиями.
 * Предоставляет методы для поиска заданий по должности и дню недели.
 */
@Repository
public interface WeeklyTaskRepository extends JpaRepository<WeeklyTask, Long> {

    /**
     * Поиск всех активных заданий для определенной должности и дня недели.
     * @param position должность
     * @param dayOfWeek день недели (1-7)
     * @return список заданий
     */
    List<WeeklyTask> findByPositionAndDayOfWeekAndActiveTrue(String position, Integer dayOfWeek);

    /**
     * Поиск всех активных заданий для должности.
     * @param position должность
     * @return список всех заданий
     */
    List<WeeklyTask> findByPositionAndActiveTrue(String position);

    /**
     * Удаление всех заданий для должности и дня недели.
     * @param position должность
     * @param dayOfWeek день недели
     */
    void deleteByPositionAndDayOfWeek(String position, Integer dayOfWeek);
}
