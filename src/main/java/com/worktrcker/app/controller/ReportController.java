package com.worktrcker.app.controller;

import com.worktrcker.app.model.WorkRecord;
import com.worktrcker.app.repository.WorkRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    
    // Отчет по сменам за период (для админа)
    @GetMapping("/report")
    public ResponseEntity<List<WorkRecord>> getWorkRecordsReport(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        List<WorkRecord> records;
        
        if (employeeId != null) {
            // Отчет по конкретному сотруднику
            records = workRecordRepository.findByEmployeeId(employeeId);
        } else {
            // Отчет по всем сотрудникам
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
}
