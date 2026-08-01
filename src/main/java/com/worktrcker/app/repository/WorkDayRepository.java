package com.worktrcker.app.repository;

import com.worktrcker.app.entity.Employee;
import com.worktrcker.app.entity.WorkDay;
import com.worktrcker.app.entity.WorkDay.WorkDayStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkDayRepository extends JpaRepository<WorkDay, Long> {
    
    // Найти рабочий день сотрудника по дате
    Optional<WorkDay> findByEmployeeAndDate(Employee employee, LocalDate date);
    
    // Найти все рабочие дни сотрудника за период
    List<WorkDay> findByEmployeeAndDateBetween(Employee employee, LocalDate startDate, LocalDate endDate);
    
    // Найти все рабочие дни за дату (для администратора)
    List<WorkDay> findByDate(LocalDate date);
    
    // Найти все рабочие дни сотрудника
    List<WorkDay> findByEmployee(Employee employee);
    
    // Найти активный рабочий день сотрудника
    Optional<WorkDay> findByEmployeeAndStatus(Employee employee, WorkDayStatus status);
}
