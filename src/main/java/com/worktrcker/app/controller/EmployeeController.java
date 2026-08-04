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
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return employeeRepository.findById(id)
            .map(employee -> {
                if (updates.containsKey("fullName")) {
                    employee.setFullName((String) updates.get("fullName"));
                }
                if (updates.containsKey("phone")) {
                    employee.setPhone((String) updates.get("phone"));
                }
                if (updates.containsKey("password")) {
                    employee.setPassword((String) updates.get("password"));
                }
                if (updates.containsKey("positionId")) {
                    Long positionId = Long.valueOf(updates.get("positionId").toString());
                    positionRepository.findById(positionId).ifPresent(employee::setPosition);
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