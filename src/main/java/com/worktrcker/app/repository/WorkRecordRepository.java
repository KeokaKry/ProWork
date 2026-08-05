package com.worktrcker.app.repository;

import com.worktrcker.app.model.WorkRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;

public interface WorkRecordRepository extends JpaRepository<WorkRecord, Long> {
    
    @EntityGraph(attributePaths = {"employee"})
    List<WorkRecord> findByEmployeeId(Long employeeId);
    
    @EntityGraph(attributePaths = {"employee"})
    List<WorkRecord> findAll();
}
