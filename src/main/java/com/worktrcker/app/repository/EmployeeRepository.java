package com.worktrcker.app.repository;

import com.worktrcker.app.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с сотрудниками.
 * Предоставляет методы для поиска сотрудников по телефону и username.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    /**
     * Поиск сотрудника по номеру телефона
     */
    Employee findByPhoneNumber(String phoneNumber);
    
    /**
     * Поиск сотрудника по username (логину)
     */
    Optional<Employee> findByUsername(String username);
}
