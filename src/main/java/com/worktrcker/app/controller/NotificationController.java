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
    // Вызывается мобильным приложением каждые 30 секунд
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
        
        // Проверка на обед (через 4 часа = 240 минут) - расширяем диапазон до 5 минут
        // Уведомляем в диапазоне 240-245 минут, но только если еще не был показан флаг lunchNotified
        Boolean lunchNotified = activeRecord.getLunchNotified();
        if (workedMinutes >= 240 && workedMinutes < 245 && (lunchNotified == null || !lunchNotified)) {
            response.put("notify", true);
            response.put("type", "LUNCH_START");
            response.put("message", "Обед начался! У вас есть 60 минут.");
            // Помечаем, что уведомление об обеде было отправлено
            activeRecord.setLunchNotified(true);
            workRecordRepository.save(activeRecord);
            return ResponseEntity.ok(response);
        }
        
        // Конец обеда (через 5 часов = 300 минут) - расширяем диапазон до 5 минут
        Boolean lunchEndNotified = activeRecord.getLunchEndNotified();
        if (workedMinutes >= 300 && workedMinutes < 305 && (lunchEndNotified == null || !lunchEndNotified)) {
            response.put("notify", true);
            response.put("type", "LUNCH_END");
            response.put("message", "Обед закончен! Пора приступать к работе.");
            activeRecord.setLunchEndNotified(true);
            workRecordRepository.save(activeRecord);
            return ResponseEntity.ok(response);
        }
        
        // Проверка на короткий перерыв (за 10 минут до конца часа) - расширяем диапазон до 3 минут
        // Уведомляем в диапазоне 50-53 минуты каждого часа
        long minutesInHour = workedMinutes % 60;
        // Проверяем, что это не первый час (workedMinutes >= 50) и уведомление еще не было показано для этого часа
        Long lastBreakMinute = activeRecord.getLastBreakMinute();
        long currentBreakHour = workedMinutes / 60;
        
        if (minutesInHour >= 50 && minutesInHour < 53 && workedMinutes >= 50) {
            // Если это новый час или уведомление еще не было показано
            if (lastBreakMinute == null || (lastBreakMinute / 60) != currentBreakHour) {
                response.put("notify", true);
                response.put("type", "SHORT_BREAK_START");
                response.put("message", "Пора отдохнуть! Перерыв 10 минут.");
                activeRecord.setLastBreakMinute(workedMinutes);
                workRecordRepository.save(activeRecord);
                return ResponseEntity.ok(response);
            }
        }
        
        // Конец короткого перерыва (ровно в 00 минут следующего часа) - расширяем диапазон до 3 минут
        Long lastBreakEndMinute = activeRecord.getLastBreakEndMinute();
        if (minutesInHour >= 0 && minutesInHour < 3 && workedMinutes > 0) {
            long breakEndHour = (workedMinutes / 60) - 1; // Предыдущий час
            // Если это новый час для окончания перерыва
            if (lastBreakEndMinute == null || (lastBreakEndMinute / 60) != breakEndHour) {
                response.put("notify", true);
                response.put("type", "SHORT_BREAK_END");
                response.put("message", "Перерыв окончен! Пора приступать к работе.");
                activeRecord.setLastBreakEndMinute(workedMinutes);
                workRecordRepository.save(activeRecord);
                return ResponseEntity.ok(response);
            }
        }
        
        response.put("notify", false);
        return ResponseEntity.ok(response);
    }
    
    // Отправка уведомления о назначении задания сотруднику
    @PostMapping("/notify-task")
    public ResponseEntity<?> notifyTaskAssignment(@RequestBody Map<String, Object> request) {
        Long employeeId = Long.valueOf(request.get("employeeId").toString());
        String task = (String) request.get("task");
        
        // В реальном приложении здесь была бы отправка push-уведомления
        // Сейчас просто логируем событие
        System.out.println("УВЕДОМЛЕНИЕ СОТРУДНИКУ #" + employeeId + ": Вам назначено задание - " + task);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Уведомление отправлено");
        
        return ResponseEntity.ok(response);
    }
}
