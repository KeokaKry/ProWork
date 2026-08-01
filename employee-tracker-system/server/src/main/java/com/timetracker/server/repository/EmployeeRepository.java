package com.timetracker.server.repository;

import com.timetracker.server.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сотрудниками.
 * Предоставляет методы для поиска, сохранения и удаления сотрудников.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Поиск сотрудника по имени пользователя (логину).
     * @param username имя пользователя
     * @return найденный сотрудник или пустой Optional
     */
    Optional<Employee> findByUsername(String username);

    /**
     * Проверка существования сотрудника по имени пользователя.
     * @param username имя пользователя
     * @return true если сотрудник существует
     */
    boolean existsByUsername(String username);

    /**
     * Поиск всех сотрудников определенной должности.
     * @param position должность
     * @return список сотрудников
     */
    List<Employee> findByPosition(String position);

    /**
     * Поиск всех сотрудников, привязанных к определенной геозоне.
     * @param geoZoneId ID геозоны
     * @return список сотрудников
     */
    List<Employee> findByGeoZoneId(Long geoZoneId);
}
