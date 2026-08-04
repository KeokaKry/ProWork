package com.worktrcker.app.controller;

import com.worktrcker.app.model.WorkRecord;
import com.worktrcker.app.repository.WorkRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private WorkRecordRepository workRecordRepository;

    // Проверка необходимости уведомления о перерыве
    // Вызывается мобильным приложением каждые несколько минут
    @GetMapping("/check-break/{employeeId}")
    public ResponseEntity<?> checkBreakNotification(@PathVariable Long employeeId) {
        Map<String, Object> response = new HashMap<>();
        
        // Получаем активную запись работы
        List<WorkRecord> records = workRecordRepository.findByEmployeeId(employeeId);
        WorkRecord activeRecord = null;
        
        for (WorkRecord record : records) {
            if ("ACTIVE".equals(record.getStatus())) {
                activeRecord = record;
                break;
            }
        }
        
        if (activeRecord == null) {
            response.put("notify", false);
            return ResponseEntity.ok(response);
        }
        
        LocalDateTime workStartTime = activeRecord.getStartTime();
        LocalDateTime now = LocalDateTime.now();
        Duration worked = Duration.between(workStartTime, now);
        long workedMinutes = worked.toMinutes();
        
        // Проверка на обед (через 4 часа = 240 минут)
        if (workedMinutes == 240) {
            response.put("notify", true);
            response.put("type", "LUNCH_START");
            response.put("message", "Обед начался! У вас есть 60 минут.");
            return ResponseEntity.ok(response);
        }
        
        if (workedMinutes == 300) {
            response.put("notify", true);
            response.put("type", "LUNCH_END");
            response.put("message", "Обед закончен! Пора приступать к работе.");
            return ResponseEntity.ok(response);
        }
        
        // Проверка на короткий перерыв (за 10 минут до конца часа)
        long minutesInHour = workedMinutes % 60;
        if (minutesInHour == 50) {
            response.put("notify", true);
            response.put("type", "SHORT_BREAK_START");
            response.put("message", "Пора отдохнуть! Перерыв 10 минут.");
            return ResponseEntity.ok(response);
        }
        
        if (minutesInHour == 0 && workedMinutes > 0) {
            response.put("notify", true);
            response.put("type", "SHORT_BREAK_END");
            response.put("message", "Перерыв окончен! Пора приступать к работе.");
            return ResponseEntity.ok(response);
        }
        
        response.put("notify", false);
        return ResponseEntity.ok(response);
    }
}
