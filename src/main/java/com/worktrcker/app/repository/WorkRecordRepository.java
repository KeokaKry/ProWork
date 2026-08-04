package com.worktrcker.app.repository;

import com.worktrcker.app.model.WorkRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkRecordRepository extends JpaRepository<WorkRecord, Long> {
    List<WorkRecord> findByEmployeeId(Long employeeId);
}