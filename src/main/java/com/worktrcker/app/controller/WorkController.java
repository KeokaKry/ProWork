package com.worktrcker.app.controller;

import com.worktrcker.app.dto.StartWorkRequest;
import com.worktrcker.app.model.WorkRecord;
import com.worktrcker.app.model.Employee;
import com.worktrcker.app.repository.WorkRecordRepository;
import com.worktrcker.app.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class WorkController {

    @Autowired
    private WorkRecordRepository workRecordRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;

    // Проверка геозоны (расстояние между двумя точками в метрах)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Радиус Земли в метрах
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // Получить все задания (список работ)
    @GetMapping("/work-records")
    public ResponseEntity<List<WorkRecord>> getAllWorkRecords() {
        return ResponseEntity.ok(workRecordRepository.findAll());
    }
    
    @PostMapping("/work/start")
    public ResponseEntity<?> startWork(@RequestBody StartWorkRequest request) {
        Optional<Employee> empOpt = employeeRepository.findById(request.getEmployeeId());
        if (empOpt.isEmpty()) return ResponseEntity.badRequest().body("Сотрудник не найден");

        Employee employee = empOpt.get();
        
        // Проверка геозоны (закомментировано для тестов)
        /*
        if (employee.getGeoZones() != null && !employee.getGeoZones().isEmpty()) {
            boolean inZone = false;
            for (var zone : employee.getGeoZones()) {
                double distance = calculateDistance(
                    request.getLatitude(), request.getLongitude(),
                    zone.getLatitude(), zone.getLongitude()
                );
                if (distance <= zone.getRadius()) {
                    inZone = true;
                    break;
                }
            }
            if (!inZone) {
                return ResponseEntity.badRequest().body("Вы находитесь вне разрешенной геозоны!");
            }
        }
        */

        WorkRecord record = new WorkRecord();
        record.setEmployee(employee);
        record.setStartTime(LocalDateTime.now());
        record.setStatus("ACTIVE");
        record.setStartLat(request.getLatitude());
        record.setStartLon(request.getLongitude());

        return ResponseEntity.ok(workRecordRepository.save(record));
    }

    @PostMapping("/work/finish/{id}")
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
    
    @GetMapping("/work/history/{employeeId}")
    public ResponseEntity<List<WorkRecord>> getHistory(@PathVariable Long employeeId) {
        return ResponseEntity.ok(workRecordRepository.findByEmployeeId(employeeId));
    }
    
    // Получить активную запись работы сотрудника
    @GetMapping("/work/active/{employeeId}")
    public ResponseEntity<?> getActiveWork(@PathVariable Long employeeId) {
        List<WorkRecord> records = workRecordRepository.findByEmployeeId(employeeId);
        for (WorkRecord record : records) {
            if ("ACTIVE".equals(record.getStatus())) {
                return ResponseEntity.ok(record);
            }
        }
        return ResponseEntity.ok(null);
    }
    
    // Получить информацию о перерывах (для уведомлений)
    @GetMapping("/work/break-info/{employeeId}")
    public ResponseEntity<?> getBreakInfo(@PathVariable Long employeeId) {
        List<WorkRecord> records = workRecordRepository.findByEmployeeId(employeeId);
        WorkRecord activeRecord = null;
        
        for (WorkRecord record : records) {
            if ("ACTIVE".equals(record.getStatus())) {
                activeRecord = record;
                break;
            }
        }
        
        if (activeRecord == null) {
            return ResponseEntity.ok(Map.of("working", false));
        }
        
        LocalDateTime startTime = activeRecord.getStartTime();
        LocalDateTime now = LocalDateTime.now();
        Duration worked = Duration.between(startTime, now);
        long workedMinutes = worked.toMinutes();
        
        // Обед через 4 часа (240 минут)
        boolean lunchTime = workedMinutes >= 240 && workedMinutes < 300;
        
        // Короткий перерыв каждые 50 минут (за 10 мин до конца часа)
        long minutesInHour = workedMinutes % 60;
        boolean shortBreakTime = minutesInHour >= 50 && minutesInHour < 60;
        
        Map<String, Object> response = new HashMap<>();
        response.put("working", true);
        response.put("workedMinutes", workedMinutes);
        response.put("lunchTime", lunchTime);
        response.put("shortBreakTime", shortBreakTime);
        
        return ResponseEntity.ok(response);
    }
}
