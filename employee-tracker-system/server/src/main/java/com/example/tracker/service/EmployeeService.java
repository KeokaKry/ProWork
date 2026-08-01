package com.example.tracker.service;

import com.example.tracker.model.Employee;
import com.example.tracker.model.GeoZone;
import com.example.tracker.model.WeeklyTask;
import com.example.tracker.repository.EmployeeRepository;
import com.example.tracker.repository.GeoZoneRepository;
import com.example.tracker.repository.WeeklyTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления сотрудниками.
 * Используется контроллером EmployeeController и AdminController.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final GeoZoneRepository geoZoneRepository;
    private final WeeklyTaskRepository weeklyTaskRepository;

    /**
     * Регистрация нового сотрудника.
     * @param fullName Полное имя сотрудника
     * @param position Должность
     * @param password Пароль
     * @return Созданный сотрудник
     */
    public Employee registerEmployee(String fullName, String position, String password) {
        // Проверка на уникальность имени
        if (employeeRepository.existsByFullName(fullName)) {
            throw new RuntimeException("Сотрудник с таким именем уже существует");
        }

        Employee employee = new Employee();
        employee.setFullName(fullName);
        employee.setPosition(position);
        employee.setPassword(password); // В реальном проекте нужно хешировать!
        
        return employeeRepository.save(employee);
    }

    /**
     * Поиск сотрудника по ID.
     * @param id ID сотрудника
     * @return Найденный сотрудник или пустой Optional
     */
    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    /**
     * Получение всех сотрудников.
     * @return Список всех сотрудников
     */
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    /**
     * Назначение геозоны сотруднику.
     * @param employeeId ID сотрудника
     * @param geoZoneId ID геозоны
     * @return Обновленный сотрудник
     */
    public Employee assignGeoZone(Long employeeId, Long geoZoneId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Сотрудник не найден"));

        GeoZone geoZone = geoZoneRepository.findById(geoZoneId)
                .orElseThrow(() -> new RuntimeException("Геозона не найдена"));

        employee.setGeoZone(geoZone);
        return employeeRepository.save(employee);
    }

    /**
     * Создание задания на день недели для сотрудника.
     * @param employeeId ID сотрудника
     * @param dayOfWeek День недели (1-Пн, ..., 7-Вс)
     * @param description Текст задания
     * @return Созданное задание
     */
    public WeeklyTask createWeeklyTask(Long employeeId, Integer dayOfWeek, String description) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Сотрудник не найден"));

        WeeklyTask task = new WeeklyTask();
        task.setEmployee(employee);
        task.setDayOfWeek(dayOfWeek);
        task.setDescription(description);

        return weeklyTaskRepository.save(task);
    }

    /**
     * Получение заданий для сотрудника на конкретный день недели.
     * @param employeeId ID сотрудника
     * @param dayOfWeek День недели (1-7)
     * @return Список заданий
     */
    public List<WeeklyTask> getTasksForDay(Long employeeId, int dayOfWeek) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Сотрудник не найден"));

        return weeklyTaskRepository.findByEmployeeAndDayOfWeek(employee, dayOfWeek);
    }
}
