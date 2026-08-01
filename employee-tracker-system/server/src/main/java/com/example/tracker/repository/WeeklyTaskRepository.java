package com.example.tracker.repository;

import com.example.tracker.model.WeeklyTask;
import com.example.tracker.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с еженедельными заданиями.
 */
@Repository
public interface WeeklyTaskRepository extends JpaRepository<WeeklyTask, Long> {

    /**
     * Поиск заданий для сотрудника на конкретный день недели.
     * @param employee Сотрудник
     * @param dayOfWeek День недели (1-7)
     * @return Список заданий
     */
    List<WeeklyTask> findByEmployeeAndDayOfWeek(Employee employee, Integer dayOfWeek);
}
