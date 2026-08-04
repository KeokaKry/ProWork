package com.worktrcker.app.controller;

import com.worktrcker.app.model.Employee;
import com.worktrcker.app.repository.EmployeeRepository;
import com.worktrcker.app.repository.PositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private PositionRepository positionRepository;

    // Получить список всех сотрудников (для выпадающего списка)
    @GetMapping("/list")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeRepository.findAllByOrderByFullNameAsc());
    }

    // Создать сотрудника (Только Админ)
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        // Проверка наличия должности, если передана ID
        if (employee.getPosition() != null && employee.getPosition().getId() != null) {
            positionRepository.findById(employee.getPosition().getId())
                .ifPresent(employee::setPosition);
        }
        return ResponseEntity.ok(employeeRepository.save(employee));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}