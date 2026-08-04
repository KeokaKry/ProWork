package com.worktrcker.app.controller;

import com.worktrcker.app.dto.LoginRequest;
import com.worktrcker.app.model.Employee;
import com.worktrcker.app.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<Employee> employeeOpt = employeeRepository.findByFullNameAndPassword(
                request.getFullName(), request.getPassword());

        if (employeeOpt.isPresent()) {
            Employee emp = employeeOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("id", emp.getId());
            response.put("fullName", emp.getFullName());
            response.put("position", emp.getPosition() != null ? emp.getPosition().getName() : "Без должности");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body("Неверное ФИО или пароль");
        }
    }
}