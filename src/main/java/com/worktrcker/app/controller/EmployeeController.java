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

    // Получить список всех сотрудников с геозонами
    @GetMapping("/list")
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeRepository.findAllWithGeoZones());
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
    
    // Обновить сотрудника (назначить должность, изменить пароль)
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee updates) {
        return employeeRepository.findById(id)
            .map(employee -> {
                if (updates.getFullName() != null) {
                    employee.setFullName(updates.getFullName());
                }
                if (updates.getPhone() != null) {
                    employee.setPhone(updates.getPhone());
                }
                if (updates.getPassword() != null) {
                    employee.setPassword(updates.getPassword());
                }
                if (updates.getPosition() != null && updates.getPosition().getId() != null) {
                    positionRepository.findById(updates.getPosition().getId()).ifPresent(employee::setPosition);
                }
                return ResponseEntity.ok(employeeRepository.save(employee));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    // Уволить сотрудника (удаление)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
