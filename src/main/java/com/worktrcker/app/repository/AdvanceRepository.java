package com.worktrcker.app.repository;

import com.worktrcker.app.model.Advance;
import com.worktrcker.app.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AdvanceRepository extends JpaRepository<Advance, Long> {

    List<Advance> findByEmployeeId(Long employeeId);

    List<Advance> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(a.amount) FROM Advance a WHERE a.employee.id = :employeeId AND a.date BETWEEN :startDate AND :endDate")
    Double sumAmountByEmployeeIdAndDateBetween(@Param("employeeId") Long employeeId, 
                                               @Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);

    List<Advance> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
