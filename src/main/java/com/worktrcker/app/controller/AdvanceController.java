package com.worktrcker.app.controller;

import com.worktrcker.app.model.Advance;
import com.worktrcker.app.service.AdvanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/advances")
@CrossOrigin(origins = "*")
public class AdvanceController {

    @Autowired
    private AdvanceService advanceService;

    // Получить все авансы
    @GetMapping
    public ResponseEntity<List<Advance>> getAllAdvances() {
        return ResponseEntity.ok(advanceService.getAllAdvances());
    }

    // Получить авансы за период
    @GetMapping("/period")
    public ResponseEntity<List<Advance>> getAdvancesByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(advanceService.getAdvancesByPeriod(startDate, endDate));
    }

    // Получить авансы сотрудника
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Advance>> getAdvancesByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(advanceService.getAdvancesByEmployee(employeeId));
    }

    // Получить авансы сотрудника за период
    @GetMapping("/employee/{employeeId}/period")
    public ResponseEntity<List<Advance>> getAdvancesByEmployeeAndPeriod(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(advanceService.getAdvancesByEmployeeAndPeriod(employeeId, startDate, endDate));
    }

    // Получить общую сумму авансов сотрудника за период
    @GetMapping("/employee/{employeeId}/total")
    public ResponseEntity<Map<String, Object>> getTotalAdvancesByEmployeeAndPeriod(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        BigDecimal total = advanceService.getTotalAdvancesByEmployeeAndPeriod(employeeId, startDate, endDate);
        Map<String, Object> response = new HashMap<>();
        response.put("employeeId", employeeId);
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        response.put("totalAmount", total);
        return ResponseEntity.ok(response);
    }

    // Создать аванс
    @PostMapping
    public ResponseEntity<Advance> createAdvance(@RequestBody Map<String, Object> request) {
        Long employeeId = Long.valueOf(request.get("employeeId").toString());
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String dateStr = (String) request.get("date");
        LocalDate date = dateStr != null ? LocalDate.parse(dateStr) : LocalDate.now();
        String comment = request.get("comment") != null ? request.get("comment").toString() : null;
        
        Advance advance = advanceService.createAdvance(employeeId, amount, date, comment);
        return ResponseEntity.ok(advance);
    }

    // Удалить аванс
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdvance(@PathVariable Long id) {
        advanceService.deleteAdvance(id);
        return ResponseEntity.ok().build();
    }
}
