package com.worktrcker.app.controller;

import com.worktrcker.app.model.WorkRecord;
import com.worktrcker.app.model.Employee;
import com.worktrcker.app.repository.WorkRecordRepository;
import com.worktrcker.app.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private WorkRecordRepository workRecordRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;

    // Админ назначает задание на день сотруднику
    @PostMapping("/assign")
    public ResponseEntity<?> assignDailyTask(@RequestBody Map<String, Object> request) {
        Long employeeId = Long.valueOf(request.get("employeeId").toString());
        String task = (String) request.get("task");
        
        Optional<Employee> empOpt = employeeRepository.findById(employeeId);
        if (empOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Сотрудник не найден");
        }
        
        // Создаем или обновляем запись работы с заданием
        WorkRecord record = new WorkRecord();
        record.setEmployee(empOpt.get());
        record.setDailyTask(task);
        record.setStatus("PENDING"); // Ожидает начала работы
        
        return ResponseEntity.ok(workRecordRepository.save(record));
    }
    
    // Обновить задание для записи работы
    @PutMapping("/{recordId}")
    public ResponseEntity<WorkRecord> updateTask(@PathVariable Long recordId, @RequestBody Map<String, String> request) {
        Optional<WorkRecord> recordOpt = workRecordRepository.findById(recordId);
        if (recordOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        WorkRecord record = recordOpt.get();
        record.setDailyTask(request.get("task"));
        return ResponseEntity.ok(workRecordRepository.save(record));
    }
    
    // Получить задания сотрудника
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<WorkRecord>> getEmployeeTasks(@PathVariable Long employeeId) {
        return ResponseEntity.ok(workRecordRepository.findByEmployeeId(employeeId));
    }
}
