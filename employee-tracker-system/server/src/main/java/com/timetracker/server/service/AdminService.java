package com.timetracker.server.service;

import com.timetracker.server.dto.EmployeeDto;
import com.timetracker.server.dto.WorkDayDto;
import com.timetracker.server.model.*;
import com.timetracker.server.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для операций Администратора.
 * Управляет должностями, геозонами, сотрудниками и табелями.
 */
@Service
@Transactional
public class AdminService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private GeoZoneRepository geoZoneRepository;

    @Autowired
    private WorkDayRepository workDayRepository;

    @Autowired
    private WorkDayService workDayService;

    // ==================== ДОЛЖНОСТИ ====================

    /**
     * Получить все должности.
     */
    public List<Position> getAllPositions() {
        return employeeRepository.findAll().stream()
                .map(Employee::getPosition)
                .distinct()
                .map(position -> {
                    Position pos = new Position();
                    pos.setName(position);
                    // В реальной системе тариф хранится в отдельной таблице
                    // Здесь заглушка - 0 рублей
                    pos.setHourlyRate(0.0);
                    return pos;
                })
                .collect(Collectors.toList());
    }

    /**
     * Создать новую должность с тарифом.
     * В упрощенной версии просто сохраняем название, тариф будем хранить в памяти
     */
    public Position createPosition(String name, Double hourlyRate) {
        Position position = new Position();
        position.setName(name);
        position.setHourlyRate(hourlyRate);
        // В реальной системе нужно сохранять в таблицу positions
        return position;
    }

    /**
     * Удалить должность (фактически помечаем как неактивную).
     */
    public void deletePosition(Long id) {
        // В упрощенной версии ничего не делаем, т.к. должности не хранятся отдельно
    }

    /**
     * Обновить тариф должности.
     */
    public Position updatePositionRate(Long id, Double hourlyRate) {
        Position position = new Position();
        position.setHourlyRate(hourlyRate);
        return position;
    }

    // ==================== ГЕОЗОНЫ ====================

    /**
     * Получить все геозоны.
     */
    public List<GeoZone> getAllGeoZones() {
        return geoZoneRepository.findAll();
    }

    /**
     * Создать новую геозону.
     */
    public GeoZone createGeoZone(String name, Double latitude, Double longitude, Integer radiusMeters) {
        GeoZone geoZone = new GeoZone();
        geoZone.setName(name);
        geoZone.setLatitude(latitude);
        geoZone.setLongitude(longitude);
        geoZone.setRadiusMeters(radiusMeters);
        return geoZoneRepository.save(geoZone);
    }

    /**
     * Удалить геозону.
     */
    public void deleteGeoZone(Long id) {
        geoZoneRepository.deleteById(id);
    }

    // ==================== СОТРУДНИКИ ====================

    /**
     * Назначить сотруднику должность и геозону.
     */
    public Employee assignEmployee(Long employeeId, Long positionId, Long geoZoneId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Сотрудник не найден"));

        // В упрощенной версии должность - это строка, поэтому просто оставляем как есть
        // В реальной системе нужно загружать Position из БД и устанавливать связь

        // Привязываем геозону
        if (geoZoneId != null) {
            GeoZone geoZone = geoZoneRepository.findById(geoZoneId)
                    .orElseThrow(() -> new RuntimeException("Геозона не найдена"));
            employee.setGeoZone(geoZone);
        }

        return employeeRepository.save(employee);
    }

    /**
     * Получить всех сотрудников в виде DTO.
     */
    public List<EmployeeDto> getAllEmployeesDto() {
        List<Employee> employees = employeeRepository.findAll();
        List<EmployeeDto> dtos = new ArrayList<>();

        for (Employee emp : employees) {
            EmployeeDto dto = new EmployeeDto();
            dto.setId(emp.getId());
            dto.setUsername(emp.getUsername());
            dto.setPosition(emp.getPosition());
            
            if (emp.getGeoZone() != null) {
                dto.setGeoZoneId(emp.getGeoZone().getId());
                dto.setGeoZoneName(emp.getGeoZone().getName());
            }
            
            dtos.add(dto);
        }

        return dtos;
    }

    /**
     * Наложить штраф на сотрудника за конкретный день.
     */
    public void applyFine(Long employeeId, LocalDate date, Double amount, String reason) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Сотрудник не найден"));

        WorkDay workDay = workDayRepository.findByEmployeeAndDate(employee, date)
                .orElseThrow(() -> new RuntimeException("Рабочий день не найден"));

        workDay.setPenalty(amount);
        workDayRepository.save(workDay);
    }

    // ==================== ТАБЕЛИ ====================

    /**
     * Получить табель учета времени.
     * Если employeeId не указан, возвращает все записи.
     */
    public List<WorkDayDto> getTimesheets(Long employeeId) {
        List<WorkDay> workDays;

        if (employeeId != null) {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Сотрудник не найден"));
            workDays = workDayRepository.findByEmployee(employee);
        } else {
            workDays = workDayRepository.findAll();
        }

        List<WorkDayDto> dtos = new ArrayList<>();

        for (WorkDay wd : workDays) {
            WorkDayDto dto = new WorkDayDto();
            dto.setId(wd.getId());
            dto.setEmployeeId(wd.getEmployee().getId());
            dto.setEmployeeName(wd.getEmployee().getUsername());
            dto.setPosition(wd.getEmployee().getPosition());
            dto.setDate(wd.getDate());
            
            if (wd.getStartTime() != null) {
                dto.setStartTime(wd.getStartTime().toLocalTime().toString());
            }
            
            if (wd.getEndTime() != null) {
                dto.setEndTime(wd.getEndTime().toLocalTime().toString());
                // Расчет отработанных часов
                double hours = calculateWorkedHours(wd);
                dto.setWorkedHours(hours);
                
                // Расчет зарплаты (тариф пока 0, в реальной системе брать из должности)
                double hourlyRate = 0.0; // TODO: получить из таблицы должностей
                dto.setHourlyRate(hourlyRate);
                dto.setCalculatedSalary(hours * hourlyRate);
            }
            
            dto.setPenalty(wd.getPenalty() != null ? wd.getPenalty() : 0.0);
            dto.setTotalSalary((dto.getCalculatedSalary() != null ? dto.getCalculatedSalary() : 0.0) - dto.getPenalty());
            dto.setComment(wd.getComment());
            
            dtos.add(dto);
        }

        return dtos;
    }

    /**
     * Расчет отработанных часов (без обеда).
     */
    private double calculateWorkedHours(WorkDay workDay) {
        if (workDay.getEndTime() == null) {
            return 0.0;
        }

        LocalDateTime start = workDay.getStartTime();
        LocalDateTime end = workDay.getEndTime();
        LocalDateTime lunchStart = workDay.getLunchStartTime();
        LocalDateTime lunchEnd = workDay.getLunchEndTime();

        long totalMinutes = ChronoUnit.MINUTES.between(start, end);

        if (lunchStart != null && lunchEnd != null) {
            if (end.isAfter(lunchStart) && start.isBefore(lunchEnd)) {
                LocalDateTime actualLunchStart = start.isBefore(lunchStart) ? lunchStart : start;
                LocalDateTime actualLunchEnd = end.isAfter(lunchEnd) ? lunchEnd : end;
                long lunchMinutes = ChronoUnit.MINUTES.between(actualLunchStart, actualLunchEnd);
                totalMinutes -= lunchMinutes;
            }
        }

        return totalMinutes / 60.0;
    }
}
