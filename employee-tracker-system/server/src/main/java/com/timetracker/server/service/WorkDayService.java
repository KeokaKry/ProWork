package com.timetracker.server.service;

import com.timetracker.server.model.Employee;
import com.timetracker.server.model.WorkDay;
import com.timetracker.server.repository.WorkDayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для управления рабочими днями.
 * Обрабатывает начало/окончание рабочего дня, перерывы, обед, расчет часов.
 */
@Service
@Transactional
public class WorkDayService {

    @Autowired
    private WorkDayRepository workDayRepository;

    /**
     * Начало рабочего дня сотрудника.
     * Автоматически рассчитывает время обеда (через 4 часа) и генерирует расписание коротких перерывов.
     * 
     * @param employee сотрудник
     * @return созданный рабочий день
     * @throws RuntimeException если у сотрудника уже есть активный рабочий день
     */
    public WorkDay startWorkDay(Employee employee) {
        // Проверка: нет ли уже активного рабочего дня
        if (workDayRepository.findByEmployeeAndActiveTrue(employee).isPresent()) {
            throw new RuntimeException("У вас уже есть активный рабочий день. Завершите его сначала.");
        }

        WorkDay workDay = new WorkDay();
        workDay.setEmployee(employee);
        workDay.setDate(LocalDate.now());
        workDay.setStartTime(LocalDateTime.now());
        workDay.setActive(true);

        // Расчет времени обеда: начинается через 4 часа после начала работы
        LocalDateTime lunchStart = workDay.getStartTime().plusHours(4);
        LocalDateTime lunchEnd = lunchStart.plusHours(1); // Обед длится 1 час
        workDay.setLunchStartTime(lunchStart);
        workDay.setLunchEndTime(lunchEnd);

        // Генерация коротких перерывов (с 50 по 60 минуту каждого часа, кроме обеда)
        List<String> shortBreaks = generateShortBreaks(workDay.getStartTime(), lunchStart, lunchEnd);
        workDay.setShortBreaks(shortBreaks);

        return workDayRepository.save(workDay);
    }

    /**
     * Генерация расписания коротких перерывов.
     * Перерывы с 50 по 60 минуту каждого часа, except во время обеда.
     * 
     * @param startTime время начала рабочего дня
     * @param lunchStart время начала обеда
     * @param lunchEnd время окончания обеда
     * @return список перерывов в формате "HH:mm-HH:mm"
     */
    private List<String> generateShortBreaks(LocalDateTime startTime, LocalDateTime lunchStart, LocalDateTime lunchEnd) {
        List<String> breaks = new ArrayList<>();
        
        // Начинаем с первого полного часа после начала работы
        LocalDateTime currentHour = startTime.withMinute(0).withSecond(0).withNano(0).plusHours(1);
        
        // Продолжаем до конца рабочего дня (пока не достигнем времени обеда или конца дня)
        while (currentHour.isBefore(lunchStart) || currentHour.isAfter(lunchEnd)) {
            LocalTime breakStartTime = LocalTime.of(currentHour.getHour(), 50);
            LocalTime breakEndTime = LocalTime.of(currentHour.getHour(), 0).plusMinutes(10);
            
            // Если это следующий час, то перерыв с 50 мин текущего часа до 10 мин следующего
            if (breakEndTime.getHour() == currentHour.getHour()) {
                breakEndTime = breakEndTime.plusHours(1);
            }
            
            // Проверяем, не попадает ли перерыв во время обеда
            LocalDateTime breakStartDT = currentHour.with(breakStartTime);
            LocalDateTime breakEndDT = currentHour.withMinute(0).plusHours(1).withMinute(10);
            
            // Если перерыв полностью вне обеда - добавляем
            if (breakEndDT.isBefore(lunchStart) || breakStartDT.isAfter(lunchEnd)) {
                String breakStr = String.format("%02d:%02d-%02d:%02d",
                        breakStartTime.getHour(), breakStartTime.getMinute(),
                        breakEndTime.getHour(), breakEndTime.getMinute());
                breaks.add(breakStr);
            }
            
            // Переходим к следующему часу
            currentHour = currentHour.plusHours(1);
            
            // Защита от бесконечного цикла (максимум 12 часов работы)
            if (currentHour.isAfter(startTime.plusHours(12))) {
                break;
            }
        }
        
        return breaks;
    }

    /**
     * Окончание рабочего дня сотрудника.
     * Рассчитывает общее количество отработанных часов (без учета обеда).
     * 
     * @param workDay рабочий день
     * @return обновленный рабочий день
     */
    public WorkDay endWorkDay(WorkDay workDay) {
        if (!workDay.isActive()) {
            throw new RuntimeException("Рабочий день уже завершен");
        }

        workDay.setEndTime(LocalDateTime.now());
        workDay.setActive(false);

        return workDayRepository.save(workDay);
    }

    /**
     * Расчет отработанных часов за день (без обеда).
     * 
     * @param workDay рабочий день
     * @return количество часов (дробное)
     */
    public double calculateWorkedHours(WorkDay workDay) {
        if (workDay.getEndTime() == null) {
            return 0.0;
        }

        LocalDateTime start = workDay.getStartTime();
        LocalDateTime end = workDay.getEndTime();
        LocalDateTime lunchStart = workDay.getLunchStartTime();
        LocalDateTime lunchEnd = workDay.getLunchEndTime();

        // Общее время с начала до конца
        long totalMinutes = ChronoUnit.MINUTES.between(start, end);

        // Вычитаем обеденный перерыв (если он попадает в рабочее время)
        if (lunchStart != null && lunchEnd != null) {
            if (end.isAfter(lunchStart) && start.isBefore(lunchEnd)) {
                LocalDateTime actualLunchStart = start.isBefore(lunchStart) ? lunchStart : start;
                LocalDateTime actualLunchEnd = end.isAfter(lunchEnd) ? lunchEnd : end;
                long lunchMinutes = ChronoUnit.MINUTES.between(actualLunchStart, actualLunchEnd);
                totalMinutes -= lunchMinutes;
            }
        }

        // Конвертируем минуты в часы
        return totalMinutes / 60.0;
    }

    /**
     * Добавление комментария к рабочему дню.
     * 
     * @param workDay рабочий день
     * @param comment текст комментария
     * @return обновленный рабочий день
     */
    public WorkDay addComment(WorkDay workDay, String comment) {
        workDay.setComment(comment);
        return workDayRepository.save(workDay);
    }

    /**
     * Назначение штрафа сотруднику за рабочий день.
     * 
     * @param workDay рабочий день
     * @param penalty сумма штрафа
     * @return обновленный рабочий день
     */
    public WorkDay setPenalty(WorkDay workDay, Double penalty) {
        workDay.setPenalty(penalty);
        return workDayRepository.save(workDay);
    }

    /**
     * Получение всех рабочих дней сотрудника за период.
     * 
     * @param employee сотрудник
     * @param startDate начальная дата
     * @param endDate конечная дата
     * @return список рабочих дней
     */
    public List<WorkDay> getWorkDaysByPeriod(Employee employee, LocalDate startDate, LocalDate endDate) {
        return workDayRepository.findByEmployeeAndDateBetween(employee, startDate, endDate);
    }

    /**
     * Получение активного рабочего дня сотрудника (если есть).
     * 
     * @param employee сотрудник
     * @return активный рабочий день или null
     */
    public WorkDay getActiveWorkDay(Employee employee) {
        return workDayRepository.findByEmployeeAndActiveTrue(employee).orElse(null);
    }
}
