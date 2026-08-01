package com.timetracker.server.service;

import com.timetracker.server.model.Employee;
import com.timetracker.server.model.GeoZone;
import com.timetracker.server.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления сотрудниками.
 * Обрабатывает бизнес-логику регистрации, обновления и удаления сотрудников.
 */
@Service
@Transactional
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private GeoDistanceService geoDistanceService;

    /**
     * Регистрация нового сотрудника.
     * @param username имя пользователя
     * @param password пароль
     * @param position должность
     * @param geoZoneId ID геозоны (объекта), где будет работать сотрудник
     * @return созданный сотрудник
     * @throws RuntimeException если имя пользователя уже занято или геозона не найдена
     */
    public Employee registerEmployee(String username, String password, String position, Long geoZoneId) {
        // Проверка: имя пользователя должно быть уникальным
        if (employeeRepository.existsByUsername(username)) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        Employee employee = new Employee();
        employee.setUsername(username);
        employee.setPassword(password); // В реальном проекте нужно хешировать пароль!
        employee.setPosition(position);

        // Если указана геозона, привязываем сотрудника к ней
        if (geoZoneId != null) {
            // Здесь должна быть проверка существования геозоны через GeoZoneRepository
            // Но пока просто создаем заглушку - в контроллере будет полная проверка
            GeoZone geoZone = new GeoZone();
            geoZone.setId(geoZoneId);
            employee.setGeoZone(geoZone);
        }

        return employeeRepository.save(employee);
    }

    /**
     * Поиск сотрудника по имени пользователя.
     * @param username имя пользователя
     * @return найденный сотрудник или пустой Optional
     */
    public Optional<Employee> findByUsername(String username) {
        return employeeRepository.findByUsername(username);
    }

    /**
     * Получение всех сотрудников.
     * @return список всех сотрудников
     */
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    /**
     * Обновление должности сотрудника.
     * @param employeeId ID сотрудника
     * @param newPosition новая должность
     * @return обновленный сотрудник
     */
    public Employee updatePosition(Long employeeId, String newPosition) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Сотрудник не найден"));
        employee.setPosition(newPosition);
        return employeeRepository.save(employee);
    }

    /**
     * Привязка сотрудника к геозоне.
     * @param employeeId ID сотрудника
     * @param geoZoneId ID геозоны
     * @return обновленный сотрудник
     */
    public Employee assignGeoZone(Long employeeId, Long geoZoneId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Сотрудник не найден"));

        // Создаем объект геозоны (в реальном коде нужна проверка через GeoZoneRepository)
        GeoZone geoZone = new GeoZone();
        geoZone.setId(geoZoneId);
        employee.setGeoZone(geoZone);

        return employeeRepository.save(employee);
    }

    /**
     * Удаление сотрудника.
     * @param employeeId ID сотрудника
     */
    public void deleteEmployee(Long employeeId) {
        employeeRepository.deleteById(employeeId);
    }

    /**
     * Проверка: находится ли сотрудник в своей геозоне.
     * @param employeeId ID сотрудника
     * @param latitude широта телефона сотрудника
     * @param longitude долгота телефона сотрудника
     * @return true если сотрудник в зоне, иначе false
     */
    public boolean isEmployeeInZone(Long employeeId, Double latitude, Double longitude) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Сотрудник не найден"));

        GeoZone geoZone = employee.getGeoZone();
        if (geoZone == null) {
            throw new RuntimeException("У сотрудника не назначена геозона");
        }

        // Вычисляем расстояние между телефоном сотрудника и центром геозоны
        double distance = geoDistanceService.calculateDistance(
                latitude, longitude,
                geoZone.getLatitude(), geoZone.getLongitude()
        );

        // Проверяем: расстояние меньше радиуса зоны?
        return distance <= geoZone.getRadiusMeters();
    }

    /**
     * Получение расстояния от сотрудника до его геозоны.
     * @param employeeId ID сотрудника
     * @param latitude широта телефона сотрудника
     * @param longitude долгота телефона сотрудника
     * @return расстояние в метрах
     */
    public double getDistanceToZone(Long employeeId, Double latitude, Double longitude) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Сотрудник не найден"));

        GeoZone geoZone = employee.getGeoZone();
        if (geoZone == null) {
            throw new RuntimeException("У сотрудника не назначена геозона");
        }

        return geoDistanceService.calculateDistance(
                latitude, longitude,
                geoZone.getLatitude(), geoZone.getLongitude()
        );
    }
}
