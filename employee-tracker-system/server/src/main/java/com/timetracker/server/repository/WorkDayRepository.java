package com.timetracker.server.repository;

import com.timetracker.server.model.WorkDay;
import com.timetracker.server.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с рабочими днями.
 * Предоставляет методы для поиска и сохранения записей о рабочих днях.
 */
@Repository
public interface WorkDayRepository extends JpaRepository<WorkDay, Long> {

    /**
     * Поиск активного рабочего дня сотрудника (если сотрудник еще не завершил день).
     * @param employee сотрудник
     * @return активный рабочий день или пустой Optional
     */
    Optional<WorkDay> findByEmployeeAndActiveTrue(Employee employee);

    /**
     * Поиск всех рабочих дней сотрудника за период.
     * @param employee сотрудник
     * @param startDate начальная дата
     * @param endDate конечная дата
     * @return список рабочих дней
     */
    List<WorkDay> findByEmployeeAndDateBetween(Employee employee, LocalDate startDate, LocalDate endDate);

    /**
     * Поиск всех рабочих дней сотрудника.
     * @param employee сотрудник
     * @return список всех рабочих дней
     */
    List<WorkDay> findByEmployee(Employee employee);

    /**
     * Поиск рабочего дня по сотруднику и дате.
     * @param employee сотрудник
     * @param date дата
     * @return рабочий день или пустой Optional
     */
    Optional<WorkDay> findByEmployeeAndDate(Employee employee, LocalDate date);
}
