package com.example.tracker.repository;

import com.example.tracker.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с сотрудниками в базе данных.
 * Предоставляет CRUD операции и методы поиска.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Проверка существования сотрудника по полному имени.
     * @param fullName Полное имя сотрудника
     * @return true если сотрудник существует
     */
    boolean existsByFullName(String fullName);

    /**
     * Поиск сотрудника по имени пользователя (логину).
     * @param username Имя пользователя
     * @return Найденный сотрудник или пустой Optional
     */
    Optional<Employee> findByUsername(String username);
}
