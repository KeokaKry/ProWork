package com.worktrcker.app.service;

import com.worktrcker.app.entity.Employee;
import com.worktrcker.app.entity.WorkDay;
import com.worktrcker.app.entity.WorkDay.WorkDayStatus;
import com.worktrcker.app.repository.WorkDayRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class WorkDayService {

    private final WorkDayRepository workDayRepository;

    public WorkDayService(WorkDayRepository workDayRepository) {
        this.workDayRepository = workDayRepository;
    }

    /**
     * Начать рабочий день
     */
    public WorkDay startWorkDay(Employee employee) {
        LocalDate today = LocalDate.now();
        
        // Проверяем, есть ли уже активный рабочий день
        var existingDay = workDayRepository.findByEmployeeAndDate(employee, today);
        if (existingDay.isPresent()) {
            throw new IllegalStateException("Рабочий день уже начат");
        }

        LocalTime startTime = LocalTime.now();
        
        WorkDay workDay = new WorkDay();
        workDay.setEmployee(employee);
        workDay.setDate(today);
        workDay.setStartTime(startTime);
        workDay.setStatus(WorkDayStatus.WORKING);
        
        // Обед начинается через 4 часа от начала работы
        workDay.setLunchStart(startTime.plusHours(4));
        workDay.setLunchEnd(workDay.getLunchStart().plusHours(1));
        
        // Генерируем короткие перерывы (с 50 по 60 минуту каждого часа, кроме обеда)
        workDay.setShortBreaks(generateShortBreaks(startTime, workDay.getLunchStart(), workDay.getLunchEnd()));
        
        return workDayRepository.save(workDay);
    }

    /**
     * Генерация коротких перерывов (с 50 по 60 минуту каждого часа)
     */
    private List<String> generateShortBreaks(LocalTime startTime, LocalTime lunchStart, LocalTime lunchEnd) {
        List<String> breaks = new ArrayList<>();
        
        int startHour = startTime.getHour();
        int endHour = lunchStart.getHour(); // До обеда
        
        for (int hour = startHour; hour < endHour; hour++) {
            // Пропускаем час обеда
            if (hour >= lunchStart.getHour() && hour < lunchEnd.getHour()) {
                continue;
            }
            
            LocalTime breakStart = LocalTime.of(hour, 50);
            LocalTime breakEnd = LocalTime.of(hour + 1, 0);
            
            // Если перерыв попадает в обед, пропускаем
            if (!breakStart.isBefore(lunchStart) && breakStart.isBefore(lunchEnd)) {
                continue;
            }
            
            breaks.add(String.format("%02d:%02d-%02d:%02d", 
                breakStart.getHour(), breakStart.getMinute(),
                breakEnd.getHour(), breakEnd.getMinute()));
        }
        
        // После обеда
        startHour = lunchEnd.getHour();
        endHour = 18; // Предполагаемый конец рабочего дня
        
        for (int hour = startHour; hour < endHour; hour++) {
            LocalTime breakStart = LocalTime.of(hour, 50);
            LocalTime breakEnd = LocalTime.of(hour + 1, 0);
            
            breaks.add(String.format("%02d:%02d-%02d:%02d", 
                breakStart.getHour(), breakStart.getMinute(),
                breakEnd.getHour(), breakEnd.getMinute()));
        }
        
        return breaks;
    }

    /**
     * Закончить рабочий день
     */
    public WorkDay finishWorkDay(Long workDayId, String comment) {
        WorkDay workDay = workDayRepository.findById(workDayId)
            .orElseThrow(() -> new RuntimeException("Рабочий день не найден"));
        
        workDay.setEndTime(LocalTime.now());
        workDay.setStatus(WorkDayStatus.FINISHED);
        workDay.setComment(comment);
        
        // Рассчитываем отработанные часы (без учета обеда)
        double workedHours = calculateWorkedHours(workDay);
        workDay.setWorkedHours(workedHours);
        
        return workDayRepository.save(workDay);
    }

    /**
     * Расчет отработанных часов (без обеда)
     */
    private double calculateWorkedHours(WorkDay workDay) {
        if (workDay.getStartTime() == null || workDay.getEndTime() == null) {
            return 0.0;
        }
        
        double totalMinutes = 0;
        
        LocalTime currentTime = workDay.getStartTime();
        LocalTime endTime = workDay.getEndTime();
        
        while (currentTime.isBefore(endTime)) {
            // Проверяем, не попадаем ли в обед
            if (currentTime.isBefore(workDay.getLunchStart())) {
                // Работаем до обеда или до конца времени
                LocalTime nextPoint = endTime.isBefore(workDay.getLunchStart()) ? endTime : workDay.getLunchStart();
                totalMinutes += java.time.Duration.between(currentTime, nextPoint).toMinutes();
                currentTime = nextPoint;
            } else if (currentTime.isBefore(workDay.getLunchEnd())) {
                // Пропускаем обед
                currentTime = workDay.getLunchEnd();
            } else {
                // После обеда
                LocalTime nextPoint = endTime;
                totalMinutes += java.time.Duration.between(currentTime, nextPoint).toMinutes();
                currentTime = nextPoint;
            }
        }
        
        return Math.round(totalMinutes / 60.0 * 100.0) / 100.0; // Округляем до 2 знаков
    }

    /**
     * Получить текущий рабочий день сотрудника
     */
    public WorkDay getCurrentWorkDay(Employee employee) {
        LocalDate today = LocalDate.now();
        return workDayRepository.findByEmployeeAndDate(employee, today)
            .orElse(null);
    }

    /**
     * Назначить штраф
     */
    public WorkDay assignPenalty(Long workDayId, Double amount, String reason) {
        WorkDay workDay = workDayRepository.findById(workDayId)
            .orElseThrow(() -> new RuntimeException("Рабочий день не найден"));
        
        workDay.setPenalty(amount);
        workDay.setPenaltyReason(reason);
        
        return workDayRepository.save(workDay);
    }
}
