package com.worktrcker.app.controller;

import com.worktrcker.app.entity.Employee;
import com.worktrcker.app.repository.EmployeeRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController означает, что этот класс будет отвечать на HTTP-запросы (из браузера или мобильного приложения)
// и возвращать данные в формате JSON.
@RestController
// @RequestMapping задает базовый адрес для всех методов внутри этого класса
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;

    // Мы "внедряем" (внедрение зависимостей / Dependency Injection) наш репозиторий через конструктор.
    // Это правильный и безопасный способ в Spring.
    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
        // Для теста: если база пустая, создадим одного сотрудника при запуске
        if (employeeRepository.count() == 0) {
            Employee testEmp = new Employee();
            testEmp.setFullName("Иванов Иван Иванович");
            testEmp.setPosition("Электрик");
            testEmp.setHourlyRate(500.0);
            testEmp.setPhoneNumber("+79001234567");
            employeeRepository.save(testEmp);
        }
    }

    // @GetMapping означает, что этот метод сработает при GET-запросе по адресу /api/employees
    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll(); // Достаем всех из базы
    }

    // @PostMapping означает, что этот метод сработает при POST-запросе (когда приложение присылает новые данные)
    @PostMapping
    public Employee createEmployee(@RequestBody Employee employee) {
        return employeeRepository.save(employee); // Сохраняем в базу и возвращаем обратно с присвоенным ID
    }
}