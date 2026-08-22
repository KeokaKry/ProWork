package com.worktrcker.app.service;

import com.worktrcker.app.model.Advance;
import com.worktrcker.app.model.Employee;
import com.worktrcker.app.repository.AdvanceRepository;
import com.worktrcker.app.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class AdvanceService {

    @Autowired
    private AdvanceRepository advanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    public Advance createAdvance(Long employeeId, BigDecimal amount, LocalDate date, String comment) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Сотрудник не найден"));
        Advance advance = new Advance(employee, amount, date != null ? date : LocalDate.now(), comment);
        return advanceRepository.save(advance);
    }

    public List<Advance> getAdvancesByEmployee(Long employeeId) {
        return advanceRepository.findByEmployeeId(employeeId);
    }

    public List<Advance> getAdvancesByEmployeeAndPeriod(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return advanceRepository.findByEmployeeIdAndDateBetween(employeeId, startDate, endDate);
    }

    public BigDecimal getTotalAdvancesByEmployeeAndPeriod(Long employeeId, LocalDate startDate, LocalDate endDate) {
        Double sum = advanceRepository.sumAmountByEmployeeIdAndDateBetween(employeeId, startDate, endDate);
        return sum != null ? BigDecimal.valueOf(sum) : BigDecimal.ZERO;
    }

    public List<Advance> getAllAdvances() {
        return advanceRepository.findAll();
    }

    public List<Advance> getAdvancesByPeriod(LocalDate startDate, LocalDate endDate) {
        return advanceRepository.findByDateBetween(startDate, endDate);
    }

    public void deleteAdvance(Long id) {
        advanceRepository.deleteById(id);
    }
}
