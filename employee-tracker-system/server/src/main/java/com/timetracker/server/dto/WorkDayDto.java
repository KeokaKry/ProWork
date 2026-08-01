package com.timetracker.server.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * DTO для передачи информации о рабочем дне (табель).
 * Используется для отображения в приложении администратора и экспорта в Excel.
 */
@Data
public class WorkDayDto {
    /**
     * ID записи рабочего дня.
     */
    private Long id;
    
    /**
     * ID сотрудника.
     */
    private Long employeeId;
    
    /**
     * Имя сотрудника.
     */
    private String employeeName;
    
    /**
     * Должность сотрудника.
     */
    private String position;
    
    /**
     * Дата рабочего дня.
     */
    private LocalDate date;
    
    /**
     * Время начала работы.
     */
    private String startTime;
    
    /**
     * Время окончания работы.
     */
    private String endTime;
    
    /**
     * Количество отработанных часов (без обеда).
     */
    private Double workedHours;
    
    /**
     * Тариф за час (из должности).
     */
    private Double hourlyRate;
    
    /**
     * Начисленная сумма (workedHours * hourlyRate).
     */
    private Double calculatedSalary;
    
    /**
     * Штраф, наложенный администратором.
     */
    private Double penalty;
    
    /**
     * Итоговая сумма к выплате (calculatedSalary - penalty).
     */
    private Double totalSalary;
    
    /**
     * Комментарий сотрудника о выполненной работе.
     */
    private String comment;
}
