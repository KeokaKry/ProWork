package com.worktrcker.app.controller;

import com.worktrcker.app.entity.Employee;
import com.worktrcker.app.repository.EmployeeRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для админ-панели.
 * Управление сотрудниками доступно только через админ-панель.
 */
@RestController
@RequestMapping("/api/admin/employees")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Получить всех сотрудников (только для админа)
     */
    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    /**
     * Создать нового сотрудника (только через админ-панель)
     */
    @PostMapping
    public Employee createEmployee(@RequestBody Employee employee) {
        // Проверяем, не занят ли username
        if (employeeRepository.findByUsername(employee.getUsername()).isPresent()) {
            throw new IllegalStateException("Пользователь с таким логином уже существует");
        }
        return employeeRepository.save(employee);
    }

    /**
     * Обновить сотрудника (только для админа)
     */
    @PutMapping("/{id}")
    public Employee updateEmployee(@PathVariable Long id, @RequestBody Employee employeeDetails) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Сотрудник не найден"));
        
        employee.setFullName(employeeDetails.getFullName());
        employee.setPosition(employeeDetails.getPosition());
        employee.setHourlyRate(employeeDetails.getHourlyRate());
        employee.setPhoneNumber(employeeDetails.getPhoneNumber());
        employee.setUsername(employeeDetails.getUsername());
        employee.setPassword(employeeDetails.getPassword());
        
        return employeeRepository.save(employee);
    }

    /**
     * Удалить сотрудника (только для админа)
     */
    @DeleteMapping("/{id}")
    public void deleteEmployee(@PathVariable Long id) {
        employeeRepository.deleteById(id);
    }
}