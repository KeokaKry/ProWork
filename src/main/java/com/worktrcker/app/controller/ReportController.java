package com.worktrcker.app.controller;

import com.worktrcker.app.model.WorkRecord;
import com.worktrcker.app.repository.WorkRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {
    
    @Autowired
    private WorkRecordRepository workRecordRepository;

    @GetMapping("/all")
    public ResponseEntity<List<WorkRecord>> getAllReports() {
        return ResponseEntity.ok(workRecordRepository.findAll());
    }
}