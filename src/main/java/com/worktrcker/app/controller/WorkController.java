package com.worktrcker.app.controller;

import com.worktrcker.app.dto.StartWorkRequest;
import com.worktrcker.app.model.WorkRecord;
import com.worktrcker.app.model.Employee;
import com.worktrcker.app.repository.WorkRecordRepository;
import com.worktrcker.app.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/work")
@CrossOrigin(origins = "*")
public class WorkController {

    @Autowired
    private WorkRecordRepository workRecordRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;

    @PostMapping("/start")
    public ResponseEntity<?> startWork(@RequestBody StartWorkRequest request) {
        Optional<Employee> empOpt = employeeRepository.findById(request.getEmployeeId());
        if (empOpt.isEmpty()) return ResponseEntity.badRequest().body("Сотрудник не найден");

        WorkRecord record = new WorkRecord();
        record.setEmployee(empOpt.get());
        record.setStartTime(LocalDateTime.now());
        record.setStatus("ACTIVE");
        record.setStartLat(request.getLatitude());
        record.setStartLon(request.getLongitude());

        return ResponseEntity.ok(workRecordRepository.save(record));
    }

    @PostMapping("/finish/{id}")
    public ResponseEntity<?> finishWork(@PathVariable Long id, @RequestBody Map<String, Double> location) {
        Optional<WorkRecord> recordOpt = workRecordRepository.findById(id);
        if (recordOpt.isEmpty()) return ResponseEntity.badRequest().body("Запись не найдена");

        WorkRecord record = recordOpt.get();
        record.setEndTime(LocalDateTime.now());
        record.setStatus("COMPLETED");
        record.setEndLat(location.get("latitude"));
        record.setEndLon(location.get("longitude"));

        return ResponseEntity.ok(workRecordRepository.save(record));
    }
    
    @GetMapping("/history/{employeeId}")
    public ResponseEntity<List<WorkRecord>> getHistory(@PathVariable Long employeeId) {
        return ResponseEntity.ok(workRecordRepository.findByEmployeeId(employeeId));
    }
}
