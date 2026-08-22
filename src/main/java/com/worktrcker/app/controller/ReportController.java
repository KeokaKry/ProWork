package com.worktrcker.app.controller;

import com.worktrcker.app.model.Advance;
import com.worktrcker.app.model.WorkRecord;
import com.worktrcker.app.repository.WorkRecordRepository;
import com.worktrcker.app.service.AdvanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {
    
    @Autowired
    private WorkRecordRepository workRecordRepository;

    @Autowired
    private AdvanceService advanceService;
    
    private static final String UPLOAD_DIR = "uploads/reports";

    // Сотрудник загружает фотоотчет о работе
    @PostMapping("/{recordId}/upload")
    public ResponseEntity<?> uploadReportPhoto(@PathVariable Long recordId, 
                                                @RequestParam("photo") MultipartFile photo) {
        Optional<WorkRecord> recordOpt = workRecordRepository.findById(recordId);
        if (recordOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Запись не найдена");
        }
        
        try {
            // Создаем директорию если не существует
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Генерируем уникальное имя файла
            String fileName = UUID.randomUUID().toString() + "_" + photo.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(photo.getInputStream(), filePath);
            
            // Сохраняем путь к фото в записи
            WorkRecord record = recordOpt.get();
            record.setReportPhotoUrl("/" + UPLOAD_DIR + "/" + fileName);
            workRecordRepository.save(record);
            
            Map<String, String> response = Map.of("photoUrl", record.getReportPhotoUrl());
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Ошибка загрузки фото: " + e.getMessage());
        }
    }
    
    // Получить все отчеты (для админа)
    @GetMapping("/all")
    public ResponseEntity<List<WorkRecord>> getAllReports() {
        return ResponseEntity.ok(workRecordRepository.findAll());
    }
    
    // Получить отчеты сотрудника
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<WorkRecord>> getEmployeeReports(@PathVariable Long employeeId) {
        return ResponseEntity.ok(workRecordRepository.findByEmployeeId(employeeId));
    }
    
    // Получить отчеты по периодам (для админа) - сгруппированные по сотрудникам
    @GetMapping("/period-report")
    public ResponseEntity<List<WorkRecord>> getPeriodReport(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        List<WorkRecord> records;
        
        if (employeeId != null) {
            records = workRecordRepository.findByEmployeeId(employeeId);
        } else {
            records = workRecordRepository.findAll();
        }
        
        // Фильтрация по датам если указаны
        if (startDate != null) {
            LocalDateTime startDateTime = LocalDateTime.parse(startDate);
            records = records.stream()
                .filter(r -> r.getStartTime() != null && !r.getStartTime().isBefore(startDateTime))
                .toList();
        }
        
        if (endDate != null) {
            LocalDateTime endDateTime = LocalDateTime.parse(endDate);
            records = records.stream()
                .filter(r -> r.getEndTime() != null && !r.getEndTime().isAfter(endDateTime))
                .toList();
        }
        
        return ResponseEntity.ok(records);
    }
    
    // Получить отчет по заданиям (для админа)
    @GetMapping("/task-report")
    public ResponseEntity<List<WorkRecord>> getTaskReport(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        List<WorkRecord> records;
        
        if (employeeId != null) {
            records = workRecordRepository.findByEmployeeId(employeeId);
        } else {
            records = workRecordRepository.findAll();
        }
        
        // Фильтрация по датам если указаны
        if (startDate != null) {
            LocalDateTime startDateTime = LocalDateTime.parse(startDate);
            records = records.stream()
                .filter(r -> r.getStartTime() != null && !r.getStartTime().isBefore(startDateTime))
                .toList();
        }
        
        if (endDate != null) {
            LocalDateTime endDateTime = LocalDateTime.parse(endDate);
            records = records.stream()
                .filter(r -> r.getEndTime() != null && !r.getEndTime().isAfter(endDateTime))
                .toList();
        }
        
        return ResponseEntity.ok(records);
    }
    
    // Скачать фотоотчет
    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadReportPhoto(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // === API для авансов ===

    // Сохранить аванс
    @PostMapping("/advance")
    public ResponseEntity<?> saveAdvance(
            @RequestParam Long employeeId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String comment) {
        try {
            Advance advance = advanceService.createAdvance(employeeId, amount, date, comment);
            return ResponseEntity.ok(advance);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ошибка сохранения аванса: " + e.getMessage());
        }
    }

    // Получить все авансы сотрудника
    @GetMapping("/advances/employee/{employeeId}")
    public ResponseEntity<List<Advance>> getEmployeeAdvances(@PathVariable Long employeeId) {
        return ResponseEntity.ok(advanceService.getAdvancesByEmployee(employeeId));
    }

    // Получить авансы сотрудника за период
    @GetMapping("/advances/employee/{employeeId}/period")
    public ResponseEntity<List<Advance>> getEmployeeAdvancesByPeriod(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(advanceService.getAdvancesByEmployeeAndPeriod(employeeId, startDate, endDate));
    }

    // Получить сумму авансов сотрудника за период
    @GetMapping("/advances/employee/{employeeId}/total")
    public ResponseEntity<BigDecimal> getEmployeeTotalAdvances(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        BigDecimal total = advanceService.getTotalAdvancesByEmployeeAndPeriod(employeeId, startDate, endDate);
        return ResponseEntity.ok(total);
    }

    // Получить все авансы (для админа)
    @GetMapping("/advances/all")
    public ResponseEntity<List<Advance>> getAllAdvances() {
        return ResponseEntity.ok(advanceService.getAllAdvances());
    }

    // Получить авансы за период (для админа)
    @GetMapping("/advances/period")
    public ResponseEntity<List<Advance>> getAdvancesByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(advanceService.getAdvancesByPeriod(startDate, endDate));
    }

    // Удалить аванс
    @DeleteMapping("/advance/{id}")
    public ResponseEntity<?> deleteAdvance(@PathVariable Long id) {
        try {
            advanceService.deleteAdvance(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ошибка удаления аванса: " + e.getMessage());
        }
    }

    // Отчет по сменам с авансами (для админа)
    @GetMapping("/work-with-advances")
    public ResponseEntity<List<Map<String, Object>>> getWorkRecordsWithAdvances(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<WorkRecord> records;
        if (employeeId != null) {
            records = workRecordRepository.findByEmployeeId(employeeId);
        } else {
            records = workRecordRepository.findAll();
        }

        // Фильтрация по датам если указаны
        if (startDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            records = records.stream()
                .filter(r -> r.getStartTime() != null && !r.getStartTime().isBefore(startDateTime))
                .toList();
        }
        
        if (endDate != null) {
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            records = records.stream()
                .filter(r -> r.getEndTime() != null && !r.getEndTime().isAfter(endDateTime))
                .toList();
        }

        // Формируем ответ с расчетом авансов и итоговой суммы
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (WorkRecord record : records) {
            Long empId = record.getEmployee().getId();
            LocalDate recStartDate = record.getStartTime().toLocalDate();
            LocalDate recEndDate = record.getEndTime() != null ? record.getEndTime().toLocalDate() : recStartDate;
            
            // Получаем сумму авансов за период работы сотрудника
            BigDecimal totalAdvances = advanceService.getTotalAdvancesByEmployeeAndPeriod(
                empId, recStartDate, recEndDate);
            
            // Расчет заработанного (часы * ставка)
            BigDecimal earned = BigDecimal.ZERO;
            BigDecimal hourlyRate = BigDecimal.ZERO;
            double hoursWorked = 0;
            if (record.getStartTime() != null && record.getEndTime() != null) {
                hoursWorked = java.time.Duration.between(record.getStartTime(), record.getEndTime()).toHours();
                // Ставку берем из позиции сотрудника
                if (record.getEmployee().getPosition() != null && 
                    record.getEmployee().getPosition().getHourlyRate() != null) {
                    hourlyRate = BigDecimal.valueOf(record.getEmployee().getPosition().getHourlyRate());
                }
                earned = hourlyRate.multiply(BigDecimal.valueOf(hoursWorked));
            }
            
            // Итоговая сумма к выдаче
            BigDecimal toPay = earned.subtract(totalAdvances);
            
            Map<String, Object> recordMap = new java.util.HashMap<>();
            recordMap.put("id", record.getId());
            recordMap.put("employeeId", empId);
            recordMap.put("employeeName", record.getEmployee().getFullName());
            recordMap.put("startTime", record.getStartTime());
            recordMap.put("endTime", record.getEndTime());
            recordMap.put("hoursWorked", hoursWorked);
            recordMap.put("hourlyRate", hourlyRate);
            recordMap.put("earned", earned);
            recordMap.put("totalAdvances", totalAdvances);
            recordMap.put("toPay", toPay);
            recordMap.put("status", record.getStatus());
            recordMap.put("reportPhotoUrl", record.getReportPhotoUrl());
            
            result.add(recordMap);
        }
        return ResponseEntity.ok(result);
    }
}
