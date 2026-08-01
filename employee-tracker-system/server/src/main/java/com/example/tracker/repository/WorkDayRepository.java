package com.example.tracker.repository;

import com.example.tracker.model.WorkDay;
import com.example.tracker.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с рабочими днями.
 */
@Repository
public interface WorkDayRepository extends JpaRepository<WorkDay, Long> {

    /**
     * Поиск рабочего дня сотрудника за конкретную дату.
     * @param employee Сотрудник
     * @param date Дата
     * @return Найденный рабочий день или пустой Optional
     */
    Optional<WorkDay> findByEmployeeAndDate(Employee employee, LocalDate date);

    /**
     * Поиск всех рабочих дней сотрудника за период.
     * @param employee Сотрудник
     * @param startDate Дата начала
     * @param endDate Дата окончания
     * @return Список рабочих дней
     */
    List<WorkDay> findByEmployeeAndDateBetween(Employee employee, LocalDate startDate, LocalDate endDate);

    /**
     * Поиск всех рабочих дней за период (для всех сотрудников).
     * @param startDate Дата начала
     * @param endDate Дата окончания
     * @return Список рабочих дней
     */
    List<WorkDay> findByDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Поиск активного рабочего дня сотрудника (еще не завершенного).
     * @param employeeId ID сотрудника
     * @param date Дата
     * @return Найденный активный рабочий день или пустой Optional
     */
    Optional<WorkDay> findByEmployeeIdAndDateAndEndTimeIsNull(Long employeeId, LocalDate date);
}
