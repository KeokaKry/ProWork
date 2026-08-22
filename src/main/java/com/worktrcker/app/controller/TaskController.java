package com.worktrcker.app.controller;

import com.worktrcker.app.model.WorkRecord;
import com.worktrcker.app.model.Employee;
import com.worktrcker.app.repository.WorkRecordRepository;
import com.worktrcker.app.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

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
        record.setStartTime(LocalDateTime.now());
        
        WorkRecord savedRecord = workRecordRepository.save(record);
        
        // Отправляем уведомление сотруднику о назначении задания
        try {
            Map<String, Object> notificationRequest = new HashMap<>();
            notificationRequest.put("employeeId", employeeId);
            notificationRequest.put("task", task);
            
            // Вызываем endpoint уведомления
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            String notificationUrl = "http://localhost:8080/api/notifications/notify-task";
            
            String jsonBody = "{\"employeeId\":" + employeeId + ",\"task\":\"" + task + "\"}";
            
            java.net.http.HttpRequest httpRequest = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(notificationUrl))
                .header("Content-Type", "application/json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
            
            client.sendAsync(httpRequest, java.net.http.HttpResponse.BodyHandlers.ofString());
            
            System.out.println("Уведомление отправлено сотруднику #" + employeeId);
        } catch (Exception e) {
            System.err.println("Ошибка отправки уведомления: " + e.getMessage());
        }
        
        // Возвращаем все задания сотрудника
        List<WorkRecord> employeeTasks = workRecordRepository.findByEmployeeId(employeeId);
        
        return ResponseEntity.ok(Map.of(
            "assignedTask", savedRecord,
            "employeeTasks", employeeTasks,
            "notificationSent", true
        ));
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
    
    // Отметить задание как выполненное (кнопка "Готово" для сотрудника)
    @PostMapping("/{recordId}/complete")
    public ResponseEntity<WorkRecord> completeTask(@PathVariable Long recordId) {
        Optional<WorkRecord> recordOpt = workRecordRepository.findById(recordId);
        if (recordOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        WorkRecord record = recordOpt.get();
        record.setStatus("COMPLETED");
        record.setEndTime(LocalDateTime.now());
        return ResponseEntity.ok(workRecordRepository.save(record));
    }
}
