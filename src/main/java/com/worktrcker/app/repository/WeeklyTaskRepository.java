package com.worktrcker.app.repository;

import com.worktrcker.app.entity.Employee;
import com.worktrcker.app.entity.WeeklyTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface WeeklyTaskRepository extends JpaRepository<WeeklyTask, Long> {
    
    // Найти все задания сотрудника
    List<WeeklyTask> findByEmployee(Employee employee);
    
    // Найти задания сотрудника по дню недели
    List<WeeklyTask> findByEmployeeAndDayOfWeek(Employee employee, DayOfWeek dayOfWeek);
    
    // Найти активные задания сотрудника
    List<WeeklyTask> findByEmployeeAndActive(Employee employee, boolean active);
}
