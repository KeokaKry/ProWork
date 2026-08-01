package com.worktrcker.app.repository;

import com.worktrcker.app.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// @Repository говорит Spring, что это компонент для работы с базой данных
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // Магия Spring Data JPA: 
    // Мы просто наследуем JpaRepository, и Spring САМ напишет за нас 
    // методы save(), findAll(), findById(), deleteById() и т.д.!
}