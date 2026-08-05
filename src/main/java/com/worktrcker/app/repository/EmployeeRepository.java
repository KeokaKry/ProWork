package com.worktrcker.app.repository;

import com.worktrcker.app.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByFullNameAndPassword(String fullName, String password);
    
    @EntityGraph(attributePaths = {"geoZones"})
    List<Employee> findAllByOrderByFullNameAsc();
    
    @EntityGraph(attributePaths = {"geoZones"})
    List<Employee> findAllWithGeoZones();
}
